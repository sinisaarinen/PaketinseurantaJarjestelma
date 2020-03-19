/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paketinseurantafinal;

import java.sql.SQLException;
import java.util.Scanner;
/**
 *
 * @author saasini
 */
public class PaketinSeurantaFinal {
    /**
     * @param args the command line arguments
     * @throws java.sql.SQLException
     */
    public static void main(String[] args) throws SQLException {
        
        Käyttöliittymä kayttis = new Käyttöliittymä();
        Scanner lukija = new Scanner(System.in);
        kayttis.aloita(lukija);
    }
}
