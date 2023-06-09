package ui;


import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.Double.min;

public class NetworkTrainer
{
    int  populationSize;
    int  numOfSurvivingParents;
    double mutationProbability;
    double stdDevForMutation;
    int maxIteration;

    public NetworkTrainer(int populationSize, int numOfSurvivingParents, double mutationProbability, double stdDevForMutation, int maxIteration) {
        this.populationSize = populationSize;
        this.numOfSurvivingParents = numOfSurvivingParents;
        this.mutationProbability = mutationProbability;
        this.stdDevForMutation = stdDevForMutation;
        this.maxIteration = maxIteration;

    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getNumOfSurvivingParents() {
        return numOfSurvivingParents;
    }

    public void setNumOfSurvivingParents(int numOfSurvivingParents) {
        this.numOfSurvivingParents = numOfSurvivingParents;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public double getStdDevForMutation() {
        return stdDevForMutation;
    }

    public void setStdDevForMutation(double stdDevForMutation) {
        this.stdDevForMutation = stdDevForMutation;
    }

    public int getMaxIteration() {
        return maxIteration;
    }

    public void setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
    }

    public Network trainNetwork(ArrayList<DataRow>data,int inputDimension, int outcomeDimension,ArrayList<Integer>RandomisedLayersDimensions)
    {
        SortedSet<Network> currentPopulation=new TreeSet<>(new NetworkComparator());
        while(currentPopulation.size()!=populationSize)
        {
            Network temp=new Network( inputDimension, outcomeDimension,RandomisedLayersDimensions);
            temp.evaluate(data);
            currentPopulation.add(temp);
        }
        for(int i=1;i<=maxIteration;i++)
        {
            SortedSet<Network> nextPopulation=extractElitist(currentPopulation);

            double totalFitness;
            totalFitness = currentPopulation.stream().mapToDouble(Network::getFitness).sum();
            double minFitness = min(currentPopulation.stream().mapToDouble(Network::getFitness).min().getAsDouble(),0);
            totalFitness+=-minFitness*currentPopulation.size();

            while(nextPopulation.size()<populationSize)
            {
                Network parent1=pickParent(currentPopulation,totalFitness,minFitness);
                Network parent2=pickParent(currentPopulation,totalFitness,minFitness);

                //to make sure we don't get same parent twice
                //Network parent2=pickParentExclude(currentPopulation,totalFitness-minFitness-parent1.getFitness(),minFitness,parent1);

                Network child=breed(parent1,parent2);
                child.evaluate(data);

                nextPopulation.add(child);
            }

            currentPopulation.clear();
            currentPopulation.addAll(nextPopulation);
            if(i%500==0)
            {
                System.out.println("[Train error @"+i+"]: "+String.format("%.6f",-currentPopulation.first().getFitness()));
            }
        }


        return currentPopulation.first();
    }

    private Network breed(Network parent1, Network parent2)
    {
        ArrayList<Layer> childHiddenLayers=new ArrayList<>();
        Layer childFinalLayer= MutateLayer(avgLayer(parent1.getFinalLayer(),parent2.getFinalLayer()));
        for(int i=0;i<parent1.getLayers().size();i++)
        {
            childHiddenLayers.add(MutateLayer(avgLayer(parent1.getLayers().get(i),parent2.getLayers().get(i))));
        }
        return  (new Network(childHiddenLayers,parent1.getInputDimension(),parent1.getOutcomeDimension(),childFinalLayer));
    }

    private Layer MutateLayer(Layer layer)
    {
        RealMatrix mutation= layer.getNeuronValues().copy();
        Random a=new Random();
        for(int i=0;i<mutation.getRowDimension();i++)
        {
            for(int j=0;j<mutation.getColumnDimension();j++)
            {
                if(a.nextDouble()<mutationProbability)
                {
                    mutation.setEntry(i,j,a.nextGaussian()*stdDevForMutation);
                }
                else
                {
                    mutation.setEntry(i,j,0);
                }
            }
        }
        mutation=mutation.add(layer.getNeuronValues());
        return (new Layer(layer.getInputDimension(), layer.getNumberOfNeurons(), mutation));
    }

    private Layer avgLayer(Layer layer1, Layer layer2)
    {
        RealMatrix matrix=layer1.getNeuronValues().copy();
        matrix=matrix.add(layer2.getNeuronValues());
        matrix=matrix.scalarMultiply(0.5);
        return (new Layer(layer1.getInputDimension(), layer1.getNumberOfNeurons(), matrix));
    }

    private Network pickParentExclude(SortedSet<Network> currentPopulation, double totalFitness, double minFitness, Network parent1)
    {
        Random a=new Random();
        double num=a.nextDouble();
        double sum=0;
        for(Network net :currentPopulation)
        {
            if(net.equals(parent1))
                continue;
            sum+=(net.getFitness()-minFitness)/totalFitness;
            if(num<sum)
            {
                return net;
            }
        }
        return currentPopulation.first();
    }

    private Network pickParent(SortedSet<Network> currentPopulation, double totalFitness, double minFitness)
    {
        Random a=new Random();
        double num=a.nextDouble();
        double sum=0;
        for(Network net :currentPopulation)
        {
            sum+=(net.getFitness()-minFitness)/totalFitness;
            if(num<sum)
            {
                return net;
            }
        }
        return currentPopulation.first();
    }

    private SortedSet<Network> extractElitist(SortedSet<Network> currentPopulation)
    {
        SortedSet<Network> newPopulation=new TreeSet<>(new NetworkComparator());
        int counter=0;
        for(Network net : currentPopulation)
        {
            newPopulation.add(net);
            counter++;
            if(counter==numOfSurvivingParents)
            {
                break;
            }
        }
        return newPopulation;
    }
}
