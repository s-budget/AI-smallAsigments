package ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//Consulted with: Leona Salihović
                //Karlo Boroš



public class Solution {

	public static void main(String... args) {

		

		int mode = 0;
		String pathStart;
		String pathComands = null;

		if (args[0].equals("cooking")) {
			pathComands = args[2];
			mode = 1;
		}
		pathStart = args[1];

		//deklaracija pocetnih parametara
		HashSet<Izraz> ulazniIzrazi = new HashSet<>();
		Izraz ciljniIzraz = null;

		Charset charset = StandardCharsets.UTF_8;
		assert pathStart != null;
		Path filePath = Paths.get(pathStart);
		try {
			List<String> lines = Files.readAllLines(filePath, charset);
			lines.removeIf(str -> str.startsWith("#") || str.length() == 0);
			for (int i = 0; i < lines.size(); i++) {
				if (mode == 0 && i == lines.size() - 1) {
					ciljniIzraz = new Izraz(lines.get(i).toLowerCase());
					break;
				}
				Izraz a = new Izraz(lines.get(i).toLowerCase());
				if (!a.isTautalogija()) {
					ulazniIzrazi.add(a);
				}
			}
		} catch (IOException ex) {
			System.out.format("I/O error: %s%n", ex);
		}

		//rjesavanje redundantnih izraza
		HashSet<Izraz> trebaIZbrisati = new HashSet<>();
		for (Izraz i : ulazniIzrazi) {
			for (Izraz u : ulazniIzrazi) {
				if (u.equals(i))
					break;
				if (i.getClanovi().containsAll(u.getClanovi())) {
					trebaIZbrisati.add(i);
					break;
				}
				if (u.getClanovi().containsAll(i.getClanovi())) {
					trebaIZbrisati.add(u);
				}
			}
		}
		for (Izraz i : trebaIZbrisati) {
			ulazniIzrazi.remove(i);
		}

		if (mode == 0) {//do resolution
			System.out.println(formatStatus(provjeriIzraz(Objects.requireNonNull(ciljniIzraz), ulazniIzrazi), ciljniIzraz.getOriginalniOblik()));
		}

		if (mode == 1) {//do cooking
			filePath = Paths.get(pathComands);
			try {
				List<String> lines = Files.readAllLines(filePath, charset);
				lines.removeIf(str -> str.startsWith("#"));
				for (String line : lines)//split stuff
				{
					String[] pair = new String[2];
					pair[0] = line.toLowerCase().substring(0,line.length()-2);
					pair[1] = String.valueOf(line.toLowerCase().charAt(line.length()-1));
					Izraz temp = new Izraz(pair[0]);
					System.out.println();
					if (pair[1].equals("-")) {
						ulazniIzrazi.remove(temp);
						System.out.println("User’s command: " + line + "\nremoved " + pair[0]);
					} else if (pair[1].equals("+"))
					{
						if (!temp.isTautalogija())
						{
							ulazniIzrazi.add(temp);
						}
						for (Izraz u : ulazniIzrazi)
						{
							if (u.equals(temp))
								break;
							if (temp.getClanovi().containsAll(u.getClanovi()))
							{
								ulazniIzrazi.remove(temp);
								break;
							}
							if (u.getClanovi().containsAll(temp.getClanovi()))
							{
								ulazniIzrazi.remove(u);
							}
						}
						System.out.println("User’s command: " + line + "\nadded " + pair[0]);
					} else if (pair[1].equals("?")) {
						System.out.println("User’s command: " + line);
						System.out.println(formatStatus(provjeriIzraz(temp, ulazniIzrazi), temp.getOriginalniOblik()));
					}
				}
			} catch (IOException ex) {
				System.out.format("I/O error: %s%n", ex);
			}
		}
	}

	public static boolean provjeriIzraz(Izraz cilj, HashSet<Izraz> ulazniSkup) {
		HashMap<String, Pair<String, String>> deductedFrom = new HashMap<>();
		HashSet<String> pocetniIzrazi = new HashSet<>();
		ulazniSkup.forEach((n) -> pocetniIzrazi.add(n.getOriginalniOblik()));

		//negiranje cilja
		HashSet<Izraz> dodatniSkup = new HashSet<>();
		HashSet<Atom> tempSet = new HashSet<>();
		cilj.getClanovi().forEach(a -> tempSet.add(new Atom(a.getOznaka(),a.isNegated())));
		for (Atom tempAtom : tempSet) {
			Izraz negiraniCilj = new Izraz(tempAtom.getOznaka(), !tempAtom.isNegated());
			dodatniSkup.add(negiraniCilj);
		}

		//kopiranje pocetnog skupa because reference and stuff
		HashSet<Izraz> pocetniSkup = new HashSet<>(ulazniSkup);


		boolean promjena = true;//dokle god se promjene desavaju trazi nove promjene
		while (promjena) {
			promjena = false;


			//za svaki izraz iz dodatnog(SOS) skupa provjeri sve pocetne izraze
			Deque<Izraz> red = new ArrayDeque<>(dodatniSkup);
			while (!red.isEmpty()) {
				Izraz dodatni1 = red.pop();
				//negiraj sve Atome iz izraza
				HashSet<Atom> negiraniUzrok = new HashSet<>();
				dodatni1.getClanovi().forEach(a -> negiraniUzrok.add(new Atom(a.getOznaka(), !a.isNegated())));
				HashSet<Izraz> trebaIzbrisati = new HashSet<>();
				for (Izraz pocetni : pocetniSkup) {
					if(trebaIzbrisati.contains(pocetni))//nema potrebe provjeravati vec iskoristene i/ili redundantne izraze
						continue;
					if(pocetni.getClanovi().stream().anyMatch(negiraniUzrok::contains)) {
						HashSet<Atom>tempset =new HashSet<>(pocetni.getClanovi());
						tempset.retainAll(negiraniUzrok);
						if (tempset.size()==1) //ako se slaze 0 onda nemozemo nist zakljuciti, ako se slaze vise reziultat je tautalogija pa nam nije bitno
						{
							HashSet<Atom> noviSet=new HashSet<>(pocetni.getClanovi());
							noviSet.addAll(dodatni1.getClanovi());
							tempset.forEach(n -> {noviSet.remove(n);noviSet.remove(n.negate());});
							if(noviSet.size()==0) //pronasli nill
							{
								deductedFrom.put("#NILL", new Pair<>(pocetni.getOriginalniOblik(), dodatni1.getOriginalniOblik()));
								IspisTrue(deductedFrom, pocetniIzrazi, cilj.getOriginalniOblik());
								return true;
							}
							if(!dodatniSkup.contains(new Izraz(noviSet)) && !pocetniSkup.contains(new Izraz(noviSet)))//ne zelimo duplice
							{

								Izraz tempIzraz = new Izraz(noviSet);
								if (!tempIzraz.isTautalogija()) {
									promjena = true;
									red.add(tempIzraz);
									dodatniSkup.add(tempIzraz);
									deductedFrom.put(tempIzraz.getOriginalniOblik(), new Pair<>(pocetni.getOriginalniOblik(), dodatni1.getOriginalniOblik()));
									for (Izraz pocetni2 : pocetniSkup)//brisanje nadizraza u pocetnom
									{
										if (pocetni2.getClanovi().size() > tempIzraz.getClanovi().size()) {
											if (pocetni2.getClanovi().containsAll(tempIzraz.getClanovi())) {
												trebaIzbrisati.add(pocetni2);
											}
										}
									}
									for (Izraz dodatni2 : dodatniSkup)//brisanje nadizraza u dodatnom
									{
										if (dodatni2.getClanovi().size() > tempIzraz.getClanovi().size()) {
											if (dodatni2.getClanovi().containsAll(tempIzraz.getClanovi())) {
												trebaIzbrisati.add(dodatni2);
												red.remove(dodatni2);
											}
										}
									}
								}
							}
						}


					}


				}
				for (Izraz i : trebaIzbrisati) {
					pocetniSkup.remove(i);
					dodatniSkup.remove(i);
					red.remove(i);
				}//Brisemo redundantne i iskoristene izraze

			}
			red= new ArrayDeque<>(dodatniSkup);
			HashSet<Izraz> trebaIzbrisati = new HashSet<>();
			//poavljamo istu stvar ali unutar SOSxSOS odnosa
			while (!red.isEmpty()) {
				Izraz dodatni1 = red.pop();
				if(trebaIzbrisati.contains(dodatni1))
					continue;
				HashSet<Atom> negiraniUzrok = new HashSet<>();
				dodatni1.getClanovi().forEach(a -> negiraniUzrok.add(new Atom(a.getOznaka(), !a.isNegated())));

				Deque<Izraz> unutarnjiRed=new ArrayDeque<>(dodatniSkup);
				while (!unutarnjiRed.isEmpty())
				{
					Izraz dodatni2 = unutarnjiRed.pop();
					if (dodatni1.equals(dodatni2) || trebaIzbrisati.contains(dodatni2))
						continue;
					if(dodatni2.getClanovi().stream().anyMatch(negiraniUzrok::contains)) {
						HashSet<Atom>tempset =new HashSet<>(dodatni2.getClanovi());
						tempset.retainAll(negiraniUzrok);
						if (tempset.size()==1) {
							HashSet<Atom> noviSet=new HashSet<>(dodatni2.getClanovi());
							noviSet.addAll(dodatni1.getClanovi());
							tempset.forEach(n -> {noviSet.remove(n);noviSet.remove(n.negate());});
							if(noviSet.size()==0) {
								deductedFrom.put("#NILL", new Pair<>(dodatni2.getOriginalniOblik(), dodatni1.getOriginalniOblik()));
								IspisTrue(deductedFrom, pocetniIzrazi, cilj.getOriginalniOblik());
								return true;
							}
							if(!dodatniSkup.contains(new Izraz(noviSet)) && !pocetniSkup.contains(new Izraz(noviSet)))//ne zelimo duplice
							{

								Izraz tempIzraz = new Izraz(noviSet);
								if (!tempIzraz.isTautalogija()) {
									promjena = true;
									red.add(tempIzraz);
									unutarnjiRed.add(tempIzraz);
									dodatniSkup.add(tempIzraz);
									deductedFrom.put(tempIzraz.getOriginalniOblik(), new Pair<>(dodatni2.getOriginalniOblik(), dodatni1.getOriginalniOblik()));
									for (Izraz pocetni2 : pocetniSkup)//brisanje nadizraza u pocetnom
									{
										if (pocetni2.getClanovi().size() > tempIzraz.getClanovi().size()) {
											if (pocetni2.getClanovi().containsAll(tempIzraz.getClanovi())) {
												trebaIzbrisati.add(pocetni2);
											}
										}
									}
									for (Izraz dodatni : dodatniSkup)//brisanje nadizraza u dodatnom
									{
										if (dodatni.getClanovi().size() > tempIzraz.getClanovi().size()) {
											if (dodatni.getClanovi().containsAll(tempIzraz.getClanovi())) {
												trebaIzbrisati.add(dodatni);
												red.remove(dodatni);
												unutarnjiRed.remove(dodatni);
											}
										}
									}
								}
							}
						}


					}

				}


			}
			for (Izraz i : trebaIzbrisati) {
				pocetniSkup.remove(i);
				dodatniSkup.remove(i);
			}


		}

		return false;
	}

	private static void IspisTrue
			(HashMap < String, Pair < String, String >> deductedFrom, HashSet < String > pocetniIzrazi,String raspad) {
		LinkedList<String> pocetni = new LinkedList<>();
		LinkedList<String> ostali = new LinkedList<>();
		LinkedList<String> ostaliPrvi=new LinkedList<>();
		Deque<String> red = new ArrayDeque<>();
		red.add("#NILL");
		while (!red.isEmpty()) {
			String formula = red.pop();
			if (pocetniIzrazi.contains(formula) || !deductedFrom.containsKey(formula)) {

				if(!deductedFrom.containsKey(formula) && !pocetniIzrazi.contains(formula) && !ostaliPrvi.contains(formula)) {
					ostaliPrvi.add(formula);
				} else {

					pocetni.add(formula);
				}
			} else {
				ostali.add(formula + " <== [ " + deductedFrom.get(formula).getFirst() + " , " + deductedFrom.get(formula).getSecond() + " ]");
				red.add(deductedFrom.get(formula).getFirst());
				red.add(deductedFrom.get(formula).getSecond());
			}
		}
		for (String i : pocetni) {
			System.out.println(i);
		}
		System.out.println("==============================================");
		for(String i:ostaliPrvi) {
			System.out.println(i+" <== [ ~( "+raspad+" ) ]");
		}

		Collections.reverse(ostali);
		for (String i : ostali) {
			System.out.println(i);
		}
	}

	public static String formatStatus ( boolean status, String statemen){
		if (status)
			return ("[CONCLUSION]: " + statemen + " is true");
		return ("[CONCLUSION]: " + statemen + " is unknown");
	}
}


class Pair<K,T>
{
	K first;
	T second;

	public Pair(K first, T second) {
		this.first = first;
		this.second = second;
	}

	public K getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Pair)) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(getFirst(), pair.getFirst()) && Objects.equals(getSecond(), pair.getSecond());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirst(), getSecond());
	}
}

class Atom
{
	String oznaka;
	boolean negated;

	public Atom(String oznaka, boolean negated) {
		this.oznaka = oznaka;
		this.negated = negated;
	}

	public Atom(String ulaz) {
		this.oznaka = ulaz.substring(ulaz.indexOf("~")+1);
		this.negated = ulaz.startsWith("~");
	}

	public String getOznaka() {
		return oznaka;
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Atom)) return false;
		Atom atom = (Atom) o;
		return isNegated() == atom.isNegated() && Objects.equals(getOznaka(), atom.getOznaka());
	}

	public Atom negate() {

		return new Atom(this.getOznaka(),!this.isNegated());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getOznaka(), isNegated());
	}

	@Override
	public String toString() {
		return  getNegForFormating(negated)+oznaka;
	}

	String getNegForFormating(boolean a) {
		if(a)
			return "~";
		return "";
	}
}

class Izraz
{
	int brojClanova;
	HashSet<Atom> clanovi=new HashSet<>();

	String originalniOblik;

	boolean tautalogija=false;

	public Izraz(String originalniOblik) {
		this.originalniOblik = originalniOblik;
		String[] blocks=originalniOblik.split(" v ");
		brojClanova=blocks.length;
		for (String i : blocks) {
			Atom a=new Atom(i);
			clanovi.add(a);
			if(clanovi.contains(a.negate())) {
				tautalogija=true;
			}
		}
	}

	public Izraz(String a,boolean b) {
		this.clanovi.add(new Atom(a,b));
		originalniOblik="";
		brojClanova=clanovi.size();
		for (Atom i : clanovi) {
			originalniOblik  =originalniOblik+" "+i+" v";

		}
		originalniOblik=originalniOblik.substring(1,originalniOblik.length()-2);

	}


	public Izraz(HashSet<Atom> clanovi) {
		this.clanovi = clanovi;
		originalniOblik="";
		brojClanova=clanovi.size();
		for (Atom i : clanovi) {
			originalniOblik  =originalniOblik+" "+i+" v";

		}
		originalniOblik=originalniOblik.substring(1,originalniOblik.length()-2);
	}

	public int getBrojClanova() {
		return brojClanova;
	}

	public HashSet<Atom> getClanovi() {
		return clanovi;
	}

	public String getOriginalniOblik() {
		return originalniOblik;
	}

	public boolean isTautalogija() {
		return tautalogija;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Izraz)) return false;
		Izraz izraz = (Izraz) o;
		return getBrojClanova() == izraz.getBrojClanova() && isTautalogija() == izraz.isTautalogija() && Objects.equals(getClanovi(), izraz.getClanovi());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBrojClanova(), getClanovi(), isTautalogija());
	}
}