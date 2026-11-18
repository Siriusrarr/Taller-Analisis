import java.util.*;

/** Aplicacion de escritorio con Swing, sin dependencias externas. */
public class AnalisisNumericoMiniDonas {
    static class Datos {
        double fijos=200000, costo=3000, meta=300000, demandaBase=300, sensibilidad=0.02;
        double a=5000, b=6000, inicial=6000, tolerancia=0.01;
        int max=1000;
        double demanda(double p) { return demandaBase-sensibilidad*p; }
        double f(double p) {
            double v=(p-costo)*demanda(p)-fijos-meta;
            if (!Double.isFinite(v)) throw new IllegalArgumentException("Desbordamiento: revise los datos.");
            return v;
        }
        double derivada(double p) { return demandaBase+sensibilidad*costo-2*sensibilidad*p; }
        void precioValido(double p) {
            if (!Double.isFinite(p) || p<0 || demanda(p)<0)
                throw new IllegalArgumentException("Precio fuera del dominio: precio y demanda deben ser no negativos.");
        }
    }
    static class Resultado {
        String metodo;
        double precio, error;
        int iteraciones;
        boolean convergio;
        List<String> filas=new ArrayList<String>();
        Resultado(String nombre) { metodo=nombre; }
    }
    static Resultado biseccion(Datos d) {
        d.precioValido(d.a); d.precioValido(d.b);
        if(d.a>=d.b) throw new IllegalArgumentException("Se requiere a < b.");
        Resultado r=new Resultado("Biseccion");
        double a=d.a,b=d.b,fa=d.f(a),fb=d.f(b);
        if(fa==0 || fb==0) { r.precio=fa==0?a:b; r.convergio=true; return r; }
        if(Math.signum(fa)==Math.signum(fb))
            throw new IllegalArgumentException("No hay cambio de signo. Ajuste el intervalo; esto no demuestra que no existan raices.");
        while((b-a)/2>d.tolerancia && r.iteraciones<d.max) {
            double m=a+(b-a)/2,fm=d.f(m);
            if(m==a || m==b) throw new IllegalArgumentException("Limite de precision numerica; aumente la tolerancia.");
            r.iteraciones++;
            r.filas.add(String.format(Locale.US,"%4d %14.6f %14.6f %14.6f %16.6f",r.iteraciones,a,b,m,fm));
            if(fm==0) { r.precio=m; r.convergio=true; return r; }
            if(Math.signum(fa)!=Math.signum(fm)) b=m;
            else { a=m; fa=fm; }
        }
        r.precio=a+(b-a)/2; r.error=(b-a)/2;
        r.convergio=r.error<=d.tolerancia;
        return r;
    }
    static Resultado newton(Datos d) {
        d.precioValido(d.inicial);
        Resultado r=new Resultado("Newton-Raphson");
        double p=d.inicial;
        r.precio=p;
        if(d.f(p)==0) { r.convergio=true; return r; }
        for(int i=1;i<=d.max;i++) {
            double pendiente=d.derivada(p);
            if(!Double.isFinite(pendiente) || Math.abs(pendiente)<1e-10)
                throw new IllegalArgumentException("Derivada nula o muy pequena. Cambie el valor inicial de Newton.");
            double siguiente=p-d.f(p)/pendiente;
            d.precioValido(siguiente);
            double residuo=d.f(siguiente);
            r.precio=siguiente; r.iteraciones=i; r.error=Math.abs(siguiente-p);
            r.filas.add(String.format(Locale.US,"%4d %18.9f %18.9f %18.9f",i,siguiente,residuo,r.error));
            if(siguiente==p && residuo!=0)
                throw new IllegalArgumentException("Estancamiento numerico. Revise los datos y la tolerancia.");
            if(residuo==0 || r.error<=d.tolerancia) { r.convergio=true; return r; }
            p=siguiente;
        }
        return r;
    }
    static class Ventana extends javax.swing.JFrame {
        private static final long serialVersionUID=1L;
        final javax.swing.JTextField[] campos=new javax.swing.JTextField[10];
        final String[] nombres={"Costos fijos mensuales ($)","Costo por mini dona ($)","Ganancia deseada ($/mes)","Demanda base A (unidades/mes)","Sensibilidad k (unidades/peso)","Bisección: extremo a","Bisección: extremo b","Newton: precio inicial","Tolerancia (pesos)","Máximo de iteraciones"};
        final String[] ejemplo={"200000","3000","300000","300","0.02","5000","6000","6000","0.01","1000"};
        final javax.swing.JTextArea resumen=new javax.swing.JTextArea();
        final javax.swing.table.DefaultTableModel tablaB=tabla(new String[]{"Iteración","a","b","Punto medio","Residuo"});
        final javax.swing.table.DefaultTableModel tablaN=tabla(new String[]{"Iteración","Precio","Residuo","Diferencia"});
        final javax.swing.JTabbedPane pestanas=new javax.swing.JTabbedPane();
        final javax.swing.JLabel estado=new javax.swing.JLabel("Ingrese los datos y seleccione un método.");
        Ventana() {
            super("Mini donas | Bisección y Newton-Raphson");
            setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            javax.swing.JPanel principal=new javax.swing.JPanel(new java.awt.BorderLayout(10,10));
            principal.setBorder(javax.swing.BorderFactory.createEmptyBorder(14,14,14,14));
            setContentPane(principal);
            javax.swing.JLabel titulo=new javax.swing.JLabel("Precio de venta de mini donas");
            titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD,20f));
            principal.add(titulo,java.awt.BorderLayout.NORTH);
            javax.swing.JPanel formulario=new javax.swing.JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints c=new java.awt.GridBagConstraints();
            c.insets=new java.awt.Insets(5,5,5,5);c.fill=java.awt.GridBagConstraints.HORIZONTAL;
            for(int i=0;i<campos.length;i++) {
                c.gridy=i;c.gridx=0;c.weightx=0;
                formulario.add(new javax.swing.JLabel(nombres[i]),c);
                campos[i]=new javax.swing.JTextField(ejemplo[i],12);
                c.gridx=1;c.weightx=1;formulario.add(campos[i],c);
                campos[i].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void insertUpdate(javax.swing.event.DocumentEvent e){invalidar();}
                    public void removeUpdate(javax.swing.event.DocumentEvent e){invalidar();}
                    public void changedUpdate(javax.swing.event.DocumentEvent e){invalidar();}
                });
            }
            c.gridy=10;c.gridx=0;c.gridwidth=2;
            formulario.add(new javax.swing.JLabel("<html>Demanda: Q(P) = A - k × P.<br>A es la demanda teórica a precio cero.<br>Sin separadores de miles: 200000.<br>Decimales: 0.02 o 0,02.</html>"),c);
            javax.swing.JPanel izquierda=new javax.swing.JPanel(new java.awt.BorderLayout());
            izquierda.add(formulario,java.awt.BorderLayout.NORTH);
            resumen.setEditable(false);resumen.setLineWrap(true);resumen.setWrapStyleWord(true);
            resumen.setMargin(new java.awt.Insets(12,12,12,12));
            pestanas.addTab("Resultados",new javax.swing.JScrollPane(resumen));
            pestanas.addTab("Bisección",new javax.swing.JScrollPane(new javax.swing.JTable(tablaB)));
            pestanas.addTab("Newton-Raphson",new javax.swing.JScrollPane(new javax.swing.JTable(tablaN)));
            javax.swing.JSplitPane division=new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,new javax.swing.JScrollPane(izquierda),pestanas);
            division.setResizeWeight(.4);division.setDividerLocation(410);
            principal.add(division,java.awt.BorderLayout.CENTER);
            javax.swing.JPanel abajo=new javax.swing.JPanel(new java.awt.BorderLayout(5,8));
            javax.swing.JPanel botones=new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,8,0));
            boton(botones,"Bisección",()->calcular(1));
            boton(botones,"Newton-Raphson",()->calcular(2));
            boton(botones,"Comparar ambos",()->calcular(3));
            boton(botones,"Restaurar ejemplo",()->{
                for(int i=0;i<campos.length;i++) campos[i].setText(ejemplo[i]);
                invalidar();estado.setText("Ejemplo restaurado. Seleccione un método.");
            });
            boton(botones,"Salir",()->dispose());
            abajo.add(botones,java.awt.BorderLayout.NORTH);abajo.add(estado,java.awt.BorderLayout.SOUTH);
            principal.add(abajo,java.awt.BorderLayout.SOUTH);
            resumen.setText("Complete los datos y pulse Comparar ambos.\n\nLas tablas de iteraciones aparecerán en sus pestañas.\n\nCaso académico: los datos son hipotéticos y la demanda es continua.");
            setSize(1060,670);setMinimumSize(new java.awt.Dimension(900,600));setLocationRelativeTo(null);
        }
        static javax.swing.table.DefaultTableModel tabla(String[] columnas) {
            return new javax.swing.table.DefaultTableModel(columnas,0) {
                private static final long serialVersionUID=1L;
                public boolean isCellEditable(int fila,int columna){return false;}
            };
        }
        void boton(javax.swing.JPanel panel,String nombre,Runnable accion) {
            javax.swing.JButton b=new javax.swing.JButton(nombre);b.addActionListener(e->accion.run());panel.add(b);
        }
        void invalidar() {
            tablaB.setRowCount(0);tablaN.setRowCount(0);resumen.setText("");
            estado.setText("Datos modificados. Vuelva a calcular.");
        }
        double numero(int i,double min,double max,boolean estricto) {
            try {
                String s=campos[i].getText().trim();
                if(!s.matches("[+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)")) throw new NumberFormatException();
                double v=Double.parseDouble(s.replace(',','.'));
                if(!Double.isFinite(v)||(estricto?v<=min:v<min)||v>max) throw new NumberFormatException();
                return v;
            } catch(NumberFormatException e) {
                campos[i].requestFocusInWindow();campos[i].selectAll();
                throw new IllegalArgumentException(nombres[i]+": ingrese un número "+(estricto?"> ":">= ")+min+" y <= "+max+", sin separadores de miles.");
            }
        }
        Datos leerDatos(int modo) {
            Datos d=new Datos();
            d.fijos=numero(0,0,1e12,false);d.costo=numero(1,0,1e12,false);d.meta=numero(2,0,1e12,false);
            d.demandaBase=numero(3,0,1e9,true);d.sensibilidad=numero(4,1e-12,1e9,false);
            double limite=d.demandaBase/d.sensibilidad;
            if(modo!=2) {d.a=numero(5,0,limite,false);d.b=numero(6,0,limite,false);}
            if(modo!=1) d.inicial=numero(7,0,limite,false);
            d.tolerancia=numero(8,0,1e9,true);
            double max=numero(9,1,10000,false);
            if(max!=Math.floor(max)) throw new IllegalArgumentException("El máximo de iteraciones debe ser un entero.");
            d.max=(int)max;return d;
        }
        Resultado resolver(Datos d,boolean b) {
            try {
                Resultado r=b?biseccion(d):newton(d);
                javax.swing.table.DefaultTableModel tabla=b?tablaB:tablaN;
                for(String fila:r.filas) tabla.addRow(fila.trim().split("\\s+"));
                resumen.append(String.format(Locale.US,"%s\n%s\nPrecio: $%.9f\nIteraciones: %d\n%s: %.9f pesos\nResiduo (ganancia - meta): $%.9f\n",
                    r.metodo,r.convergio?"Criterio de parada alcanzado.":"NO CONVERGIÓ: límite de iteraciones alcanzado.",r.precio,r.iteraciones,b?"Semiancho del intervalo":"Diferencia sucesiva",r.error,d.f(r.precio)));
                if(r.convergio) resumen.append(String.format(Locale.US,"Demanda estimada: %.4f unidades/mes\nGanancia estimada: $%.4f\n",d.demanda(r.precio),d.f(r.precio)+d.meta));
                resumen.append("\n");return r;
            } catch(IllegalArgumentException e) {
                resumen.append((b?"Bisección":"Newton-Raphson")+": "+e.getMessage()+"\n\n");return null;
            }
        }
        void calcular(int modo) {
            invalidar();pestanas.setSelectedIndex(0);
            try {
                Datos d=leerDatos(modo);
                Resultado b=modo!=2?resolver(d,true):null;
                Resultado n=modo!=1?resolver(d,false):null;
                if(modo==3 && b!=null && n!=null && b.convergio && n.convergio) {
                    resumen.append(String.format(Locale.US,"COMPARACIÓN\nBisección: %d iteraciones | Newton: %d iteraciones\nDiferencia entre precios: $%.9f\n\n",b.iteraciones,n.iteraciones,Math.abs(b.precio-n.precio)));
                    if(Math.abs(b.precio-n.precio)>2*d.tolerancia) resumen.append("Los precios difieren. Revise los residuos; pueden ser raíces distintas.\n");
                }
                resumen.append("El semiancho de Bisección acota el error. La diferencia sucesiva de Newton no garantiza la misma cota.\nDatos hipotéticos y demanda continua; redondear precios o cantidades cambia la ganancia.");
                boolean ok=(modo==2||(b!=null&&b.convergio))&&(modo==1||(n!=null&&n.convergio));
                estado.setText(ok?"Cálculo completado. Consulte los resultados y las tablas.":"No se completaron todos los cálculos. Revise los mensajes.");
                resumen.setCaretPosition(0);
            } catch(IllegalArgumentException e) {
                estado.setText("Revise los datos del formulario.");
                javax.swing.JOptionPane.showMessageDialog(this,e.getMessage(),"Dato inválido",javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(()->{
            try { javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName()); }
            catch(Exception ignored) { /* Se conserva el aspecto predeterminado de Swing. */ }
            new Ventana().setVisible(true);
        });
    }
}
