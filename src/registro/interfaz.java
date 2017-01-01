package registro;
import Clases.ConexionBD;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Color;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class interfaz extends javax.swing.JFrame {
//Creacion de variables privadas
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet featuresverificacion;
    public ConexionBD con = new ConexionBD();

    public DPFPTemplate getTemplate() {
        return template;
    }
    public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template ;
        this.template =template;
        firePropertyChange(TEMPLATE_PROPERTY,old,template);
    }   
    /**
     * Constrcutor de interfaz JFrame modificado
     * @param String[] args Array con los parametros pasados desde el exterior
     */
    
    DefaultListModel modelo = new DefaultListModel();
    public interfaz( String[] args ) {
        try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         JOptionPane.showMessageDialog(null, "Imposible modificar el tema visual", "Lookandfeel inválido.",
         JOptionPane.ERROR_MESSAGE);
         }

        initComponents();
        toFront();
        setExtendedState(MAXIMIZED_BOTH);
        txtArea2.setEditable(false);
        busqueda.setVisible(false);
        
        String user = args[0];
        String curso = args[1];
        
        usertxt.setText(user);
        cursotxt.setText(curso);
        cursotxt.setVisible(false);
        idalumnotxt.setVisible(false);
        
        getcursos();
        
        lista.setModel(modelo);
        solicitudes();
        
        setIconImage(new ImageIcon(getClass().getResource("/img/ITACA.png")).getImage() );
    }
    public void getcursos(){
     try {
            Connection c1=con.conectar();

            String cursox = cursotxt.getText();
            
            PreparedStatement cursoStmt = c1.prepareStatement("SELECT DISTINCT mt.Nombre as materia, mt.Clave as clave, cc.carrera as carrera, pd.periodo as periodo FROM asignados ag INNER JOIN materias mt ON mt.idmasignada= ag.idmasignada INNER JOIN docentes dc ON dc.iddocente = ag.iddocente INNER JOIN cursos cc ON cc.idcurso = mt.Curso INNER JOIN periodos pd ON pd.idperiodos = cc.periodo WHERE pd.estado = '1' AND cc.idcurso = '" + cursox +" ' GROUP BY cc.idcurso");
            ResultSet rst = cursoStmt.executeQuery();

            //Si se encuentra el nombre en la base de datos
            if(rst.next() == true){
                String materia=rst.getString("materia");
                String clave=rst.getString("clave");
                String carrera=rst.getString("carrera");
                
                this.materiatxt.setText(materia);
                this.clavetxt.setText(clave);
                this.carreratxt.setText(carrera);
            }
        } catch (SQLException e) {
            //Si ocurre un error lo indica en la consola
            JOptionPane.showMessageDialog(null, "Error en la Consulta", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
        }finally{
            con.desconectar();
        }       
    } 
    private void solicitudes(){
        try {
        //Establece los valores para la sentencia SQL
       Connection c1=con.conectar();
        
       String idcurso = cursotxt.getText();
       String docente = usertxt.getText();
       
       //Obtiene todas las huellas de la bd
       PreparedStatement solStmt = c1.prepareStatement("SELECT DISTINCT al.Nombres as Nombre, al.Apellidos as Apellidos, al.Num_Control as numc FROM curso_alumnos ca INNER JOIN cursos cc ON cc.idcurso = ca.idcurso INNER JOIN asignados ag INNER JOIN periodos pd ON pd.idperiodos = cc.periodo INNER JOIN docentes dc ON dc.iddocente = ag.iddocente INNER JOIN alumnos al ON al.idAlum = ca.idalumno WHERE pd.estado = '1' AND ca.idcurso = '"+ idcurso +"' AND dc.nombre_profesores = '"+ docente +"' AND ca.aprobado = '0' GROUP BY ca.idcursoal");
       ResultSet rs = solStmt.executeQuery();

       //Si se encuentra el nombre en la base de datos
       while(rs.next() == true){
           String nombre = rs.getString("Nombre");
           String apellidos = rs.getString("Apellidos");
           String ncontrol = rs.getString("numc");
           
           String alumno = " - "+nombre+" "+apellidos;
           modelo.addElement(ncontrol+alumno);
       }

       } catch (SQLException e) {
       //Si ocurre un error lo indica en la consola
       JOptionPane.showMessageDialog(null, "Error en la Consulta", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
       }finally{
       con.desconectar();
       }
    }
    public void aprobado(){
        String curso = cursotxt.getText();
        String id = idalumnotxt.getText();
     try {
     //Establece los valores para la sentencia SQL
     Connection c=con.conectar();
        try (
            PreparedStatement cursoStmt = c.prepareStatement("UPDATE curso_alumnos SET aprobado = '1' WHERE idalumno = ? AND idcurso = ?")) {
            cursoStmt.setString(1,id);
            cursoStmt.setString(2,curso);
  
            //Ejecuta la sentencia
            cursoStmt.execute();
            
            int pos= lista.getSelectedIndex();
            modelo.remove(pos);
            
            nombretxt.setText("- - - - -");
            apellidotxt.setText("- - - - -");
            idalumnotxt.setText("");
            verificadotxt.setText("- - - - - - - -");
            
        }
            
        } catch (SQLException ex) {
            //Si ocurre un error lo indica en la consola
            JOptionPane.showMessageDialog(null,"Error");
        }finally{
       con.desconectar();
       }
   }
    public void guardarHuella(){
    //Obtiene los datos del template de la huella actual
    ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
     Integer tamañoHuella=template.serialize().length;

     //Pregunta el nombre de la persona a la cual corresponde dicha huella
     String numcontrol = idalumnotxt.getText();
     try {
     //Establece los valores para la sentencia SQL
     Connection c=con.conectar();
        try (
            PreparedStatement guardarStmt = c.prepareStatement("UPDATE alumnos SET huella = ?, verificado = ? WHERE idAlum = ?")) {
            guardarStmt.setBinaryStream(1, datosHuella,tamañoHuella);
            guardarStmt.setInt(2,1);
            guardarStmt.setString(3,numcontrol);
            //Ejecuta la sentencia
            guardarStmt.execute();
        }
            aprobado();
            btnGuardar.setEnabled(false);
            
        } catch (SQLException ex) {
            //Si ocurre un error lo indica en la consola
            JOptionPane.showMessageDialog(null,"Error");
        }finally{
       con.desconectar();
       }
   }
    public void identificarHuella(){
        try {
       //Establece los valores para la sentencia SQL
       Connection c=con.conectar();
       String idAl = idalumnotxt.getText(); 
       //Obtiene todas las huellas de la bd
       PreparedStatement identificarStmt = c.prepareStatement("SELECT huella, Num_Control FROM alumnos WHERE idAlum ='"+ idAl +"'");
       ResultSet rs = identificarStmt.executeQuery();

       //Si se encuentra el nombre en la base de datos
       while(rs.next()){
       //Lee la plantilla de la base de datos
       byte templateBuffer[] = rs.getBytes("huella");
       String nombre=rs.getString("Num_Control");
       DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
       setTemplate(referenceTemplate);

       DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());

       if (result.isVerified()){
       //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
           aprobado();
       JOptionPane.showMessageDialog(null, nombre+" Se ha agregado al cuerso","Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
       return;
                               }
       }
       //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
       JOptionPane.showMessageDialog(null, "No existen coincidencias", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
       setTemplate(null);
       } catch (SQLException e) {
       //Si ocurre un error lo indica en la consola
       System.err.println("Error al identificar huella dactilar."+e.getMessage());
       }finally{
       con.desconectar();
       }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    protected void Iniciar(){
    Lector.addDataListener(new DPFPDataAdapter(){
    @Override public void dataAcquired(final DPFPDataEvent e){
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
    EnviarTexto("La muestra ha sido Capturada");
    ProcesarCaptura(e.getSample());
    }});
    }
    });
    Lector.addReaderStatusListener(new DPFPReaderStatusAdapter(){
    @Override public void readerConnected(final DPFPReaderStatusEvent e){
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
        EnviarTexto("Estado del Lector [Conectado]");
        }});
    }
    @Override public void readerDisconnected(final DPFPReaderStatusEvent e){
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
    EnviarTexto("Estado del Lector [Desconectado]");
    }});
    }
    });
    
    Lector.addSensorListener(new DPFPSensorAdapter(){
       @Override public void fingerTouched(final DPFPSensorEvent e){
       SwingUtilities.invokeLater(new Runnable(){ public void run(){
       //EnviarTexto("El dedo ha sido colocado sobre el lector de huella");
       }});} 
    });
    Lector.addErrorListener(new DPFPErrorAdapter(){
    public void errorReader(final DPFPErrorEvent e){
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
    EnviarTexto("Error: "+e.getError());
    }});}
    });
    Lector.addErrorListener(new DPFPErrorAdapter(){
    public void errorReader(final DPFPErrorEvent e){
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
    EnviarTexto("Error: "+e.getError());
    }});}
    });
    }
    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
    DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try{
                return extractor.createFeatureSet(sample, purpose);
            }catch(DPFPImageQualityException e){
            return null;
            }
    }    
    public Image CrearImagenHuella(DPFPSample sample){
    return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    public  void ProcesarCaptura(DPFPSample sample){
 // Procesar la muestra de la huella y crear un conjunto de características con el propósito de inscripción.
 featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

 // Procesar la muestra de la huella y crear un conjunto de características con el propósito de verificacion.
 featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

 // Comprobar la calidad de la muestra de la huella y lo añade a su reclutador si es bueno
 if (featuresinscripcion != null)
     try{
     System.out.println("Las Caracteristicas de la Huella han sido creada");
     Reclutador.addFeatures(featuresinscripcion);// Agregar las caracteristicas de la huella a la plantilla a crear

     // Dibuja la huella dactilar capturada.
     Image image=CrearImagenHuella(sample);
     DibujarHuella(image);

     }catch (DPFPImageQualityException ex) {
     System.err.println("Error: "+ex.getMessage());
     }

     finally {
     EstadoHuellas();
     // Comprueba si la plantilla se ha creado.
	switch(Reclutador.getTemplateStatus())
        {
            case TEMPLATE_STATUS_READY:	// informe de éxito y detiene  la captura de huellas
	    stop();
            setTemplate(Reclutador.getTemplate());
	    EnviarTexto("La Plantilla de la Huella ha Sido Creada, ya puede Verificarla o Identificarla");
            btnGuardar.setEnabled(true);
            btnGuardar.grabFocus();
            break;

	    case TEMPLATE_STATUS_FAILED: // informe de fallas y reiniciar la captura de huellas
	    Reclutador.clear();
            stop();
	    EstadoHuellas();
	    setTemplate(null);
	 //ojo aqui   JOptionPane.showMessageDialog(Registro.this, "La Plantilla de la Huella no pudo ser creada, Repita el Proceso", "Inscripcion de Huellas Dactilares", JOptionPane.ERROR_MESSAGE);
	    start();
	    break;
	}
	     }
}
    public void DibujarHuella(Image image){
    lblImagenHuella2.setIcon(new ImageIcon(image.getScaledInstance(lblImagenHuella2.getWidth(),lblImagenHuella2.getHeight(), Image.SCALE_DEFAULT)));
    repaint();
    }  
    public void EstadoHuellas(){
    EnviarTexto("Muestras Necesarias: " + Reclutador.getFeaturesNeeded());
    }
    public void EnviarTexto(String string){
    txtArea2.append(string + "\n");
    }
    public void start(){
    Lector.startCapture();;
    EnviarTexto("Coloque su dedo en el Lector");
    }
    public void stop(){
    Lector.stopCapture();
    EnviarTexto("El sistema ha sido pausado");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        salir = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        usertxt = new javax.swing.JLabel();
        materiatxt = new javax.swing.JLabel();
        cursotxt = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        clavetxt = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        carreratxt = new javax.swing.JLabel();
        lblImagenHuella2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea2 = new javax.swing.JTextArea();
        btnGuardar = new javax.swing.JButton();
        verificadotxt = new javax.swing.JLabel();
        apellidotxt = new javax.swing.JLabel();
        nombretxt = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        busqueda = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lista = new javax.swing.JList();
        jToggleButton1 = new javax.swing.JToggleButton();
        capturabtx = new javax.swing.JToggleButton();
        idalumnotxt = new javax.swing.JLabel();
        verificarbtx = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 3, 14)); // NOI18N
        jLabel1.setText("Bienvenido:");

        salir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        salir.setText("Salir");
        salir.setToolTipText("");
        salir.setAutoscrolls(true);
        salir.setFocusPainted(false);
        salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salirActionPerformed(evt);
            }
        });

        usertxt.setFont(new java.awt.Font("Tahoma", 2, 18)); // NOI18N
        usertxt.setText("usertxt");

        materiatxt.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        materiatxt.setText("jLabel2");

        cursotxt.setText("jLabel2");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("MATERIA:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("CLAVE:");

        clavetxt.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        clavetxt.setText("jLabel2");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("CARRERA:");

        carreratxt.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        carreratxt.setText("jLabel2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cursotxt))
                    .addComponent(materiatxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(salir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usertxt, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(clavetxt, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4))
                                .addGap(50, 50, 50)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(carreratxt, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 11, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(cursotxt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usertxt)
                .addGap(7, 7, 7)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materiatxt)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(clavetxt)
                            .addComponent(carreratxt))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(salir, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        txtArea2.setColumns(20);
        txtArea2.setRows(5);
        jScrollPane1.setViewportView(txtArea2);

        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/save.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        verificadotxt.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
        verificadotxt.setText("- - - - -");
        verificadotxt.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        apellidotxt.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
        apellidotxt.setText("- - - - -");

        nombretxt.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
        nombretxt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nombretxt.setText("- - - - -");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Datos del Alumno:");

        busqueda.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
        busqueda.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " SOLICITUDES: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 14))); // NOI18N

        lista.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lista.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(lista);

        jToggleButton1.setText("Eliminar Solicitud");

        capturabtx.setText("Captura de Huella");
        capturabtx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capturabtxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(capturabtx, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(capturabtx, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        idalumnotxt.setText("jLabel6");

        verificarbtx.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/list.png"))); // NOI18N
        verificarbtx.setText("Verificar");
        verificarbtx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificarbtxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(busqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblImagenHuella2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                    .addComponent(btnGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(verificarbtx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(verificadotxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(nombretxt, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(apellidotxt, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(0, 125, Short.MAX_VALUE)))
                                .addGap(10, 10, 10))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(idalumnotxt)
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(busqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nombretxt, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(apellidotxt, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verificadotxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(verificarbtx, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblImagenHuella2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 191, Short.MAX_VALUE)
                .addComponent(idalumnotxt)
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_salirActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarHuella();
        Reclutador.clear();
        lblImagenHuella2.setIcon(null);
        start();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void capturabtxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capturabtxActionPerformed
        int index = lista.getSelectedIndex();
        if (index>=0){
            busqueda.setText(lista.getSelectedValue().toString());
            String numcontrol = busqueda.getText();
            try {
            //Establece los valores para la sentencia SQL
            Connection c1=con.conectar();

            
           // busqueda.setVisible(false);

            //Obtiene todas las huellas de la bd
            PreparedStatement buscarStmt = c1.prepareStatement("SELECT * FROM alumnos WHERE Num_Control = '" + numcontrol +" ' ");
            ResultSet rs = buscarStmt.executeQuery();

            //Si se encuentra el nombre en la base de datos
            while(rs.next() == true){
                String nombre=rs.getString("Nombres");
                String apellidos=rs.getString("Apellidos");
                String id = rs.getString("idAlum");
                int verificado = rs.getInt("verificado");
                
                nombretxt.setText(nombre);
                apellidotxt.setText(apellidos);
                idalumnotxt.setText(id);
                
                if(verificado == 1){
                    Iniciar();
                    start();
                    
                    verificadotxt.setForeground(Color.red);
                    verificadotxt.setText("HUELLA YA CAPTURADA, VERIFIQUE SU HUELLA");
                    btnGuardar.setEnabled(false);
                }
                if(verificado == 0){
                    verificadotxt.setForeground(Color.black); 
                    verificadotxt.setText("SIN CAPTURA, COLOQUE SU DEDO EN EL LECTOR");
                        Iniciar();
                        start();
                        EstadoHuellas();
                        verificarbtx.setEnabled(false);
                }
                
               
            }
        } catch (SQLException e) {
            //Si ocurre un error lo indica en la consola
            JOptionPane.showMessageDialog(null, "Error en la Consulta", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
        }finally{
            con.desconectar();
        }
            
        }
        else{
            JOptionPane.showMessageDialog(null, "Seleccione un alumno para continuar", "LAVCIBAS", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_capturabtxActionPerformed

    private void verificarbtxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarbtxActionPerformed
        identificarHuella();
    }//GEN-LAST:event_verificarbtxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                String args[] = new String[2];
                new interfaz( args ).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel apellidotxt;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JTextField busqueda;
    private javax.swing.JToggleButton capturabtx;
    private javax.swing.JLabel carreratxt;
    private javax.swing.JLabel clavetxt;
    private javax.swing.JLabel cursotxt;
    private javax.swing.JLabel idalumnotxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel lblImagenHuella2;
    private javax.swing.JList lista;
    private javax.swing.JLabel materiatxt;
    private javax.swing.JLabel nombretxt;
    private javax.swing.JButton salir;
    private javax.swing.JTextArea txtArea2;
    private javax.swing.JLabel usertxt;
    private javax.swing.JLabel verificadotxt;
    private javax.swing.JToggleButton verificarbtx;
    // End of variables declaration//GEN-END:variables
}
