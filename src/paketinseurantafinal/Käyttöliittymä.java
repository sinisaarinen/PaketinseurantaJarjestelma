/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paketinseurantafinal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author saasini
 */
public class Käyttöliittymä {
    
    private Connection db;
    
    public Käyttöliittymä() throws SQLException {
        this.db = DriverManager.getConnection("jdbc:sqlite:testi.db");
    }
    
    public void aloita(Scanner lukija) throws SQLException {
        
        Statement s = this.db.createStatement();
        
        while (true) {
            System.out.println("Valitse toiminto (1-9 tai X): ");
            System.out.println(" X - lopeta");
            System.out.println(" 1 - luo tietokanta");
            System.out.println(" 2 - lisää paikka");
            System.out.println(" 3 - lisää asiakas");
            System.out.println(" 4 - lisää paketti");
            System.out.println(" 5 - lisää tapahtuma");
            System.out.println(" 6 - hae paketin tapahtumat");
            System.out.println(" 7 - hae asiakkaan paketit");
            System.out.println(" 8 - hae paikan tapahtumat");
            System.out.println(" 9 - suorita tehokkuustesti");
            System.out.println("");

            String komento = lukija.nextLine();
            
            if (komento.equals("X")) {
                break;
            } else if (komento.equals("1")) {
                lisaaTietokanta(lukija, s);
            } else if (komento.equals("2")) {
                lisaaPaikka(lukija, s);
            } else if (komento.equals("3")) {
                lisaaAsiakas(lukija, s);
            } else if (komento.equals("4")) {
                lisaaPaketti(lukija, s);
            } else if (komento.equals("5")) {
                lisaaTapahtuma(lukija, s);
            } else if (komento.equals("6")) {
                haePaketinTapahtumat(lukija, s);
            } else if (komento.equals("7")) {
                haeAsiakkaanPaketit(lukija, s);
            } else if (komento.equals("8")) {
                haePaikanTapahtumat(lukija, s);
            } else if (komento.equals("9")) {
                tehokkuusTesti(s);
                poistaTiedotJaLuoIndeksit(s);
                tehokkuusTesti(s);
            } else {
                System.out.println("Tuntematon komento.");
            }
        }
    }
    
    public void lisaaTietokanta(Scanner lukija, Statement s) {
        try {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("CREATE TABLE Paikat (paikka_id INTEGER PRIMARY KEY, nimi TEXT UNIQUE)");
            s.execute("CREATE TABLE Asiakkaat (asiakas_id INTEGER PRIMARY KEY, nimi TEXT UNIQUE)");
            s.execute("CREATE TABLE Paketit (paketti_id INTEGER PRIMARY KEY, paketti_koodi TEXT UNIQUE, asiakas_id INTEGER REFERENCES Asiakkaat)");
            s.execute("CREATE TABLE Tapahtumat (paketti_id INTEGER REFERENCES Paketit, paikka_id INTEGER REFERENCES Paikat, kuvaus TEXT, pvm TEXT, aika TEXT)");
            System.out.println("Tietokanta luotu.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Tietokanta on jo lisätty.");
        }
    }
    
    public void lisaaPaikka(Scanner lukija, Statement s) {
        System.out.println("Anna paikan nimi: ");
        String paikka = lukija.nextLine();
        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Paikat(nimi) VALUES (?)");
            p.setString(1,paikka);
            p.executeUpdate();
            System.out.println("Paikka lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikka on jo olemassa.");
        }
    }
    
    public void lisaaAsiakas(Scanner lukija, Statement s) {
        System.out.println("Anna asiakkaan nimi: ");
        String asiakas = lukija.nextLine();
        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Asiakkaat(nimi) VALUES (?)");
            p.setString(1,asiakas);
            p.executeUpdate();
            System.out.println("Asiakas lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakas on jo olemassa.");
        }
    }
    
    public void lisaaPaketti(Scanner lukija, Statement s) {
        System.out.println("Anna paketin seurantakoodi: ");
        String seurantakoodi = lukija.nextLine();
        System.out.println("Anna asiakkaan nimi: ");
        String asiakas = lukija.nextLine();
        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Paketit(paketti_koodi, asiakas_id) VALUES (?,?)");
            p.setString(1,seurantakoodi);
            PreparedStatement a = db.prepareStatement("SELECT asiakas_id FROM Asiakkaat WHERE nimi=?");
            a.setString(1,asiakas);
            ResultSet r = a.executeQuery();
            int id = r.getInt("asiakas_id");
            p.setInt(2,id);
            p.executeUpdate();
            System.out.println("Paketti lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakasta ei löydy tai paketti on jo olemassa.");
        }
    }
    
    public void lisaaTapahtuma(Scanner lukija, Statement s) {
        System.out.println("Anna paketin seurantakoodi: ");
        String seurantakoodi = lukija.nextLine();
        System.out.println("Anna tapahtuman paikka: ");
        String paikka = lukija.nextLine();
        System.out.println("Anna tapahtuman kuvaus: ");
        String kuvaus = lukija.nextLine();
        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Tapahtumat(paketti_id, paikka_id, kuvaus, pvm, aika) VALUES (?,?,?,?,?)");
            PreparedStatement b = db.prepareStatement("SELECT paketti_id FROM Paketit WHERE paketti_koodi=?");
            b.setString(1,seurantakoodi);
            ResultSet re = b.executeQuery();
            int id1 = re.getInt("paketti_id");
            p.setInt(1, id1);
            PreparedStatement a = db.prepareStatement("SELECT paikka_id FROM Paikat WHERE nimi=?");
            a.setString(1,paikka);
            ResultSet r = a.executeQuery();
            int id = r.getInt("paikka_id");
            p.setInt(2, id);
            p.setString(3, kuvaus);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
            DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("HH:mm:ss"); 
            LocalDateTime now = LocalDateTime.now();
            String pvm = dtf.format(now);
            String aika = dtf2.format(now);
            p.setString(4, pvm);
            p.setString(5, aika);
            p.executeUpdate();
            System.out.println("Tapahtuma lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikkaa tai seurantakoodia ei löydy.");
        }
    }
    
    public void haePaketinTapahtumat(Scanner lukija, Statement s) {
        System.out.println("Anna paketin seurantakoodi: ");
        String seurantakoodi = lukija.nextLine();
        try {
            PreparedStatement b = db.prepareStatement("SELECT paketti_id FROM Paketit WHERE paketti_koodi=?");
            b.setString(1,seurantakoodi);
            ResultSet re = b.executeQuery();
            int id1 = re.getInt("paketti_id");
            PreparedStatement p = db.prepareStatement("SELECT Tapahtumat.pvm, Tapahtumat.aika, Paikat.nimi, Tapahtumat.kuvaus FROM Tapahtumat JOIN Paikat ON Tapahtumat.paikka_id=Paikat.paikka_id WHERE Tapahtumat.paketti_id=?");
            p.setInt(1, id1);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                System.out.println(r.getString("pvm")+" "+r.getString("aika")+", "+r.getString("nimi")+", "+r.getString("kuvaus"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Seurantakoodia ei löydy.");
        }
    }
    
    public void haeAsiakkaanPaketit(Scanner lukija, Statement s) {
        System.out.println("Anna asiakkaan nimi: ");
        String asiakas = lukija.nextLine();
        try {
            PreparedStatement p = db.prepareStatement("SELECT Paketit.paketti_koodi, COALESCE(COUNT(Tapahtumat.kuvaus), 0) as lkm FROM Paketit LEFT JOIN Tapahtumat ON Paketit.paketti_id=Tapahtumat.paketti_id WHERE Paketit.asiakas_id=? GROUP BY Paketit.paketti_koodi");
            PreparedStatement c = db.prepareStatement("SELECT asiakas_id FROM Asiakkaat WHERE nimi=?");
            c.setString(1,asiakas);
            ResultSet res = c.executeQuery();
            int id2 = res.getInt("asiakas_id");
            p.setInt(1,id2);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                System.out.println(r.getString("paketti_koodi")+", "+r.getInt("lkm")+" tapahtuma(a) ");
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakasta ei löydy.");
        }
    }
    
    public void haePaikanTapahtumat(Scanner lukija, Statement s) {
        System.out.println("Anna paikan nimi: ");
        String paikka = lukija.nextLine();
        System.out.println("Anna päivämäärä (muodossa YYYY/MM/DD): ");
        String pvm = lukija.nextLine();
        try {
            PreparedStatement d = db.prepareStatement("SELECT paikka_id FROM Paikat WHERE nimi=?");
            d.setString(1,paikka);
            ResultSet resu = d.executeQuery();
            int id3 = resu.getInt("paikka_id"); 
            PreparedStatement p = db.prepareStatement("SELECT COALESCE(COUNT(Tapahtumat.kuvaus), 0) as lkm FROM Tapahtumat WHERE Tapahtumat.paikka_id=? AND Tapahtumat.pvm=?");
            p.setInt(1, id3);
            p.setString(2, pvm);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                System.out.println(r.getInt("lkm")+" tapahtumaa");
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikkaa tai valittua päivämäärää ei löydy.");
        }
    }
    
    public void tehokkuusTesti(Statement s) throws SQLException {
        System.out.println("Suoritetaan tehokkuustesti.");
              
        s.execute("BEGIN TRANSACTION");
        Random random = new Random();
                
        //1. vaihe               
        long t1aika1 = System.nanoTime();        
        PreparedStatement p1 = db.prepareStatement("INSERT INTO Paikat(nimi) VALUES (?)");
        for (int i=1; i<=1000; i++) {
            p1.setString(1, "P"+i);
            p1.executeUpdate();
        }        
        long t1aika2 = System.nanoTime();        
        System.out.println("Aikaa kului "+(t1aika2-t1aika1)/1e9+" sekuntia");
        
        //2. vaihe        
        long t2aika1 = System.nanoTime();        
        PreparedStatement p2 = db.prepareStatement("INSERT INTO Asiakkaat(nimi) VALUES (?)");        
        for (int i=1; i<=1000; i++) {        
            p2.setString(1, "A"+i);        
            p2.executeUpdate();        
        }        
        long t2aika2 = System.nanoTime();        
        System.out.println("Aikaa kului "+(t2aika2-t2aika1)/1e9+" sekuntia");
        
        //3. vaihe        
        long t3aika1 = System.nanoTime();        
        PreparedStatement p3 = db.prepareStatement("INSERT INTO Paketit(paketti_koodi, asiakas_id) VALUES (?,(SELECT asiakas_id FROM Asiakkaat WHERE nimi=?))");        
        for (int i=1; i<=1000; i++) {        
            int numero = random.nextInt(1000)+1;        
            p3.setString(1, "PP"+i);        
            p3.setString(2, "A"+numero);        
            p3.executeUpdate();        
        }        
        long t3aika2 = System.nanoTime();        
        System.out.println("Aikaa kului "+(t3aika2-t3aika1)/1e9+" sekuntia");
        
        //4. vaihe        
        long t4aika1 = System.nanoTime();        
        PreparedStatement p4 = db.prepareStatement("INSERT INTO Tapahtumat(paketti_id) VALUES ((SELECT paketti_id FROM Paketit WHERE paketti_koodi=?))");        
        for (int i=1; i<=1000000; i++) {        
            int numero = random.nextInt(1000)+1;        
            p4.setString(1, "PP"+numero);        
            p4.executeUpdate();        
        }        
        long t4aika2 = System.nanoTime();        
        System.out.println("Aikaa kului "+(t4aika2-t4aika1)/1e9+" sekuntia");        
        s.execute("COMMIT");
        
        //5. vaihe        
        s.execute("BEGIN TRANSACTION");        
        long t5aika1 = System.nanoTime();        
        PreparedStatement p5 = db.prepareStatement("SELECT COALESCE(COUNT(paketti_koodi), 0) FROM Paketit WHERE asiakas_id=?");        
        for (int i=1; i<=1000; i++) {        
            int numero = random.nextInt(1000)+1;        
            p5.setInt(1,i);        
            p5.executeQuery();        
        }        
        long t5aika2 = System.nanoTime();        
        System.out.println("Aikaa kului "+(t5aika2-t5aika1)/1e9+" sekuntia");        
        s.execute("COMMIT");
        
        //6. vaihe        
        s.execute("BEGIN TRANSACTION");        
        long t6aika1 = System.nanoTime();        
        PreparedStatement p6 = db.prepareStatement("SELECT COALESCE(COUNT(paketti_id), 0) FROM Tapahtumat WHERE paketti_id=?");        
        for (int i=1; i<=1000; i++) {        
            int numero1 = random.nextInt(1000)+1;
            p6.setInt(1,numero1);
            p6.executeQuery();
        }
        long t6aika2 = System.nanoTime();
        System.out.println("Aikaa kului "+(t6aika2-t6aika1)/1e9+" sekuntia");
        s.execute("COMMIT");
    }
    
    public void poistaTiedotJaLuoIndeksit(Statement s) throws SQLException {
        PreparedStatement poisto1 = db.prepareStatement("DELETE FROM Tapahtumat");
        PreparedStatement poisto2 = db.prepareStatement("DELETE FROM Paketit");        
        PreparedStatement poisto3 = db.prepareStatement("DELETE FROM Paikat");        
        PreparedStatement poisto4 = db.prepareStatement("DELETE FROM Asiakkaat");        
        poisto1.executeUpdate();        
        poisto2.executeUpdate();        
        poisto3.executeUpdate();        
        poisto4.executeUpdate();        
        PreparedStatement indeksit1 = db.prepareStatement("CREATE INDEX paikka_index ON Paikat(nimi)");        
        PreparedStatement indeksit2 = db.prepareStatement("CREATE INDEX nimi_index ON Asiakkaat(nimi)");        
        PreparedStatement indeksit3 = db.prepareStatement("CREATE INDEX koodi_index ON Paketit(paketti_koodi)");        
        PreparedStatement indeksit4 = db.prepareStatement("CREATE INDEX paketti_id_index ON Tapahtumat(paketti_id)");        
        indeksit1.executeUpdate();        
        indeksit2.executeUpdate();        
        indeksit3.executeUpdate();        
        indeksit4.executeUpdate();
    }
}
