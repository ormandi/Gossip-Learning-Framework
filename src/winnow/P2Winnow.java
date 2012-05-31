package winnow;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * A www.cs.cmu.edu/~vitor/papers/onlinetechreport.pdf
 * cikk alapjan megvalositott Positive Winnow algoritmus
 * elosztott kornyezetben. Predikciohoz a belso szorzatot 
 * szamitja ki, majd osszehasonlitja egy kuszobertekkel
 * (ez jelen esetben 0.5). A modell javitas soran a
 * passziv-agressziv megkozelitest hasznalja: helyes
 * osztalyozas eseten nem csinal semmit, helytelen esetben
 * elolepteti a false negative, es bunteti a false pozitiv
 * mintak szerint a sulyokat. Az eloleptetes es buntetes
 * merteket 1+eta alakban hataroztam meg, ahol az eta egy
 * konfiguracios fajlbol beolvashato parameter.
 * @author sborde
 *
 */
public class P2Winnow implements Model  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A sulyvektor, amit tanulunk. A multiplikativ
	 * modell miatt 1.0-re kell inicializalnunk, ezt
	 * a initialized adattagbol tudjuk.
	 */
	private SparseVector w;

	/**
	 * Egy {0,1} vektor, amely azt jelzi, hogy a
	 * sulyvektor adott koordinatajat inicializaltuk-e
	 * már. Ha igen, akkor az adott helyen 1 all, es 
	 * ettol kezdve csak modositjuk.
	 */
	private SparseVector initialized;
	
	/**
	 * Osztalyok szama.
	 */
	private int numberOfClasses;
	
	/**
	 * Az eta erteke. Ez a tanulo konstans. Parameterbol
	 * kapjuk, alapertelmezett erteke 5.0, ez teljesitett
	 * a legjobban.
	 */
	protected double eta = 0.1;
	
	/**
	 * Parameter neve a konfig fajlban.
	 */
	protected static final String PAR_ETA = "eta";
	
	/**
	 * Default konstruktor. Letrehozza a vektorokat.
	 */
	public P2Winnow() {
		setNumberOfClasses(2);
		w = new SparseVector();
		initialized = new SparseVector();
	}
	
	/**
	 * Konstruktor, ami parameterul varja az osztalyok szamat,
	 * majd letrehozza a vektorokat.
	 * @param numberOfClasses osztalyok szama
	 */
	public P2Winnow(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
		w = new SparseVector();
		initialized = new SparseVector();
	}
	
	/**
	 * "Copy" konstruktor, letrehozza a deep copyt a clone metodus szamara.
	 * Parameterul varja az osszes adattagot.
	 * @param w masolando sulyvektor
	 * @param initialized inicializalast jelolo vektor
	 * @param numberOfClasses osztalyok szama
	 * @param eta a tanulo konstans
	 */
	public P2Winnow(SparseVector w, SparseVector initialized, int numberOfClasses, double eta) {
		this.numberOfClasses = numberOfClasses;
		this.w = (SparseVector)w.clone();
		this.initialized = (SparseVector)initialized.clone();
		this.eta = eta;
	}
	
	public Object clone(){
		return new P2Winnow(w, initialized, numberOfClasses, eta);
	}
	
	@Override
	public void init(String prefix) {
		eta = Configuration.getDouble(prefix + "." + PAR_ETA, 5.0);
		w = new SparseVector();
	}

	@Override
	public void update(SparseVector instance, double label) {
		double y = (label==0.0)?-1.0:1.0;		//helyes cimke, atvaltjuk {-1,1}-be
		double y_pred = (predict(instance)==0.0)?-1.0:1.0;		//altalunk predikalt cimke, itt is atterunk {-1,1}-re
		if ( y != y_pred ) {					//teves osztalyozas eseten javitjuk a sulyt (agressziv mod)
			for ( VectorEntry ve : instance ) {	//ellenorizzuk, hogy inicializalva volt-e mar az i. suly
				if ( initialized.get(ve.index) == 0.0 ) {	//ha nem, megtesszuk
					initialized.put(ve.index, 1.0);
					w.put(ve.index, 1.0);
				}
				w.put(ve.index, w.get(ve.index)*Math.pow((1+eta), y));	//majd vegrehajtjuk az eloleptetest/lefokozast az eredeti cimketol fuggoen
			}
		}
	}

	@Override
	public double predict(SparseVector instance) {
		double innerProduct = w.mul(instance);	//kiszamitja a belso szorzatat a sulyoknak es a jellemzoknek
		double n = 0.5;							//kuszobertek
		return (innerProduct > n)?1.0:0.0;		//ha a kuszoberteknel nagyobb a szorzat, akkor igaznak vesszuk
	}

	@Override
	public int getNumberOfClasses() {
		return this.numberOfClasses;
	}

	@Override
	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;		
	}

	
}

