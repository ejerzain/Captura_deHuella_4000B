/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package registro;


/**
 *
 * @author Erick
 */
import javax.swing.JOptionPane;
public class Main {

    public static void main(String[] args) {
        try{          
            //LLama a la interfaz pasando el array de parametros que entra por pa lariable args
            new interfaz( args ).setVisible(true);            
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {            
            //si no existen parametros muestra error y termina programa
            JOptionPane.showMessageDialog(null, "Acceso Denegado", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}
