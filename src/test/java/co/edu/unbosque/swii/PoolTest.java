/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.unbosque.swii;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.pool2.BaseObjectPool;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.testng.annotations.Test;

/**
 *
 * @author Alejandro
 */
public class PoolTest {

    public static final String pwd = "YckGwYC8r3";

    @Test(expectedExceptions = org.postgresql.util.PSQLException.class,
            expectedExceptionsMessageRegExp = ".*too many connections.*"
    )
    public void soloDebeCrear5Conexiones() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        for (int i = 0; i < 6; i++) {
            pool.borrowObject();
        }
    }

    @Test
    public void aprendiendoAControlarLasConexiones() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        for (int i = 0; i < 6; i++) {
            Connection c = pool.borrowObject();
            pool.returnObject(c);
        }
    }

    @Test(expectedExceptions = org.postgresql.util.PSQLException.class,
            expectedExceptionsMessageRegExp = ".*connection has been closed.*"
    )
    public void quePasaCuandoSeCierraUnaConexionAntesDeRetornarla() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        Connection c = pool.borrowObject();
        c.close();
        pool.returnObject(c);
        c.createStatement().executeQuery("SELECT 1");
    }

    @Test
    public void quePasaCuandoSeRetornaUnaconexionContransaccionIniciada() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        Connection c = pool.borrowObject();
        c.setAutoCommit(false);
        PreparedStatement pst = null;
        pst = c.prepareStatement("UPDATE tbl_hilos set hilo='Batch-2' WHERE hilo = 'Batch-2.1' and registro=86");
        pst.execute();
        pool.returnObject(c);
        pst.execute();
        c.commit();

    }

    @Test(threadPoolSize = 5, invocationCount = 5)
    public void midaTiemposParaInsertar1000RegistrosConSingleton() {

    }

    @Test(threadPoolSize = 5, invocationCount = 5)
    public void midaTiemposParaInsertar1000RegistrosConObjectPool() {

    }
}
