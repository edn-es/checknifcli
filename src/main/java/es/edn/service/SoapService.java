package es.edn.service;

import com.ctc.wstx.exc.WstxException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.bugc.jdit.ws.comprecequivent_xsd.CompRecEquivEnt;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.bugc.jdit.ws.comprecequivent_xsd.CompRecEquivEntE;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.bugc.jdit.ws.comprecequivu_wsdl.CompRecEquivUServiceCompRecEquivUPort3Stub;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.burt.jdit.ws.vnifv2_wsdl.VNifV2ServiceStub;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.burt.jdit.ws.vnifv2ent_xsd.Contribuyente_type0;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.burt.jdit.ws.vnifv2ent_xsd.VNifV2Ent;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.burt.jdit.ws.vnifv2ent_xsd.VNifV2EntE;
import io.micronaut.context.annotation.Context;
import jakarta.inject.Singleton;
import org.apache.axiom.om.DeferredParsingException;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.edn.model.Contribuyente;
import es.edn.config.SslFactory;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Context
public class SoapService {

    private static final Logger logger = LoggerFactory.getLogger(SoapService.class);
    private static final String IDENTIFICADO = "IDENTIFICADO";
    private static final String SIMILAR = "NO IDENTIFICADO-SIMILAR";
    private static final String NO_IDENTIFICADO = "NO IDENTIFICADO";

    private final VNifV2ServiceStub vNifV2ServiceStub;
    private final CompRecEquivUServiceCompRecEquivUPort3Stub recEquivStub;

    public SoapService(SslFactory sslFactory) throws Exception {
        this.vNifV2ServiceStub = new VNifV2ServiceStub();
        this.vNifV2ServiceStub._getServiceClient()
                .getOptions()
                .setProperty(HTTPConstants.CACHED_HTTP_CLIENT, sslFactory.sslEnabledHttpClient());

        this.recEquivStub = new CompRecEquivUServiceCompRecEquivUPort3Stub();
        this.recEquivStub._getServiceClient()
                .getOptions()
                .setProperty(HTTPConstants.CACHED_HTTP_CLIENT, sslFactory.sslEnabledHttpClient());
    }

    public List<Contribuyente>checkNif(List<Contribuyente> list){
        var types = list.stream().map(l->{
            var contribuyenteType0 = new Contribuyente_type0();
            contribuyenteType0.setNif(l.nif());
            contribuyenteType0.setNombre(l.nombre());
            return contribuyenteType0;
        }).toList().toArray(new Contribuyente_type0[0]);

        var ent = new VNifV2Ent();
        ent.setContribuyente(types);

        var query = new VNifV2EntE();
        query.setVNifV2Ent(ent);

        try {
            var sal = vNifV2ServiceStub.vNifV2(query).getVNifV2Sal();
            return Arrays.stream(sal.getContribuyente()).map(e->{
                boolean ok = IDENTIFICADO.equalsIgnoreCase(e.getResultado());
                var ret = new Contribuyente(
                        e.getNif(), e.getNombre(), ok, e.getResultado()
                );
                return ret;
            }).toList();
        }catch(Exception e){
            logger.info("Error checking nifs", e);
            return List.of();
        }
    }

    public Contribuyente checkNif(String nif, String apellido) {

        var contribuyenteType0 = new Contribuyente_type0();
        contribuyenteType0.setNif(nif);
        contribuyenteType0.setNombre(apellido);

        var ent = new VNifV2Ent();
        ent.setContribuyente( new Contribuyente_type0[]{contribuyenteType0} );

        var query = new VNifV2EntE();
        query.setVNifV2Ent(ent);

        try {
            var sal = vNifV2ServiceStub.vNifV2(query).getVNifV2Sal();
            boolean ok = IDENTIFICADO.equalsIgnoreCase(sal.getContribuyente()[0].getResultado());
            String result = sal.getContribuyente()[0].getResultado();
            return new Contribuyente(nif, sal.getContribuyente()[0].getNombre().trim(), ok, result);
        }catch(Exception e){
            logger.info("Error checking nif {}, {}", nif, apellido, e);
            return new Contribuyente(nif, apellido, false, e.getMessage());
        }
    }

    public Contribuyente checkRecargoEquivalencia(String nif){
        var contribuyenteType0 = new es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.bugc.jdit.ws.comprecequivent_xsd.Contribuyente_type0();
        contribuyenteType0.setNif(nif);
        contribuyenteType0.setNombre(" ");

        var ent = new CompRecEquivEnt();
        ent.setContribuyente(contribuyenteType0);

        var query = new CompRecEquivEntE();
        query.setCompRecEquivEnt(ent);

        try {
            logger.info("{}",nif);
            var sal = recEquivStub.compRecEquivU(query).getCompRecEquivSal();
            boolean ok = sal.getContribuyente().getResultado().startsWith("NIF sometido al");
            String result = sal.getContribuyente().getResultado();
            return new Contribuyente(nif, "", ok, result);
        } catch (Exception e) {
            //logger.info("Error checking recargo {}, {}", nif, "", e);
            return new Contribuyente(nif, "", false, e.getMessage());
        }
    }

}
