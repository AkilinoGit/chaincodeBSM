package com.bsm.chaincode;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "LoteSetter",
        info = @Info(
                title = "LoteSetter contract",
                description = "Contract to set info of lotes",
                version = "0.0.1"))
@Default
public final class LoteSetter implements ContractInterface {

    private final Genson genson = new Genson();

    private enum LoteSetterErrors {
        LOTE_NOT_FOUND,
        LOTE_ALREADY_EXISTS,
        OPERATION_NOT_AUTHORIZED
    }
    private enum Orgs {
        Org1MSP,
        Org2MSP

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AssetLote registrarLote(final Context ctx, final String id, final String producto, final String kg, final String origen) {
        //Solo puede registrar la ORG1
        ChaincodeStub stub = ctx.getStub();
        if (!ctx.getClientIdentity().getMSPID().toLowerCase().equals(Orgs.Org1MSP.toString().toLowerCase())) {
            String errorMessage = String.format("Es necesario pertenecer a la Org1, you are:" + ctx.getClientIdentity().getMSPID().toLowerCase() + "We want:" + Orgs.Org1MSP.toString().toLowerCase()  , ctx.getClientIdentity().getMSPID());
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.OPERATION_NOT_AUTHORIZED.toString());
        }

        String state = stub.getStringState(id);

        if (!state.isEmpty()) {
            String errorMessage = String.format("Lote ya registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.LOTE_ALREADY_EXISTS.toString());
        }

        List<String> intermediarios = new ArrayList<>();
        //Obtener nombre de peer a través de su certificado obtenido así: byte[] creator = stub.getCreator();

        AssetLote lote = new AssetLote(id, producto, kg,"","", origen, intermediarios);

        String newState = genson.serialize(lote);

        stub.putStringState(id, newState);

        return lote;
    }
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String transportarLote(final Context ctx, final String id, final String km) {
        //Solo puede transportar la ORG2
        ChaincodeStub stub = ctx.getStub();
         if (!ctx.getClientIdentity().getMSPID().toLowerCase().equals(Orgs.Org2MSP.toString().toLowerCase())) {
            String errorMessage = String.format("Es necesario pertenecer a la Org2, you are:" + ctx.getClientIdentity().getMSPID().toLowerCase() + "We want:" + Orgs.Org2MSP.toString().toLowerCase()  , ctx.getClientIdentity().getMSPID());
            System.out.println(errorMessage);
             throw new ChaincodeException(errorMessage, LoteSetterErrors.OPERATION_NOT_AUTHORIZED.toString());
         }

        String state = stub.getStringState(id);

        if (state.isEmpty()) {
            String errorMessage = String.format("Lote no registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.LOTE_NOT_FOUND.toString());
        } 
        AssetLote asset = genson.deserialize(state, AssetLote.class);
        //List<String> listaIntermediarios = asset.getIntermediarios();
        // Obtener PeerTransporte listaIntermediarios.add(newOwner); 
        AssetLote newAsset =
        new AssetLote(asset.getId(),asset.getProduct(),asset.getKg(),km,asset.getPrecioKgFinal(),asset.getOrigen(),asset.getIntermediarios());

        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(id, sortedJson);

        return km + "Km transportados por la empresa : " /*Añadir nombre peer*/;
    }
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String venderLote(final Context ctx, final String id, final String precio) {
        //Solo puede vender la ORG3
        ChaincodeStub stub = ctx.getStub();

        String state = stub.getStringState(id);

        if (state.isEmpty()) {
            String errorMessage = String.format("Lote no registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.LOTE_NOT_FOUND.toString());
        } 
        AssetLote asset = genson.deserialize(state, AssetLote.class);
        //List<String> listaIntermediarios = asset.getIntermediarios();
        // Obtener PeerTransporte listaIntermediarios.add(newOwner); 
        AssetLote newAsset =
        new AssetLote(asset.getId(),asset.getProduct(),asset.getKg(),asset.getKm(),precio,asset.getOrigen(),asset.getIntermediarios());

        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(id, sortedJson);

        return "Lote de " + asset.getProduct() + "en venta a " + precio + "euros/kg";
    }
    /*
    CREAR SETTERS
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void loteVendido(final Context ctx, final String id) {
        //Solo ORG3
        ChaincodeStub stub = ctx.getStub();

        String state = stub.getStringState(id);

        if (state.isEmpty() || state == null) {
            String errorMessage = String.format("Lote no registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.LOTE_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public AssetLote imprimirLote(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String state = stub.getStringState(id);

        if (state.isEmpty() || state == null) {
            String errorMessage = String.format("Lote no registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.LOTE_NOT_FOUND.toString());
        }

        AssetLote lote = genson.deserialize(state, AssetLote.class);
        return lote;
    }
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String listarLotes(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<AssetLote> queryResults = new ArrayList<AssetLote>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            AssetLote asset = genson.deserialize(result.getStringValue(), AssetLote.class);
            System.out.println(asset);
            queryResults.add(asset);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String verIdentidad(final Context ctx) {
        return "Client identity MSPID: " + ctx.getClientIdentity().getMSPID() + "Client ID: " + ctx.getClientIdentity().getId();
    }
    //"Client identity MSPID: "Org1MSPClient "Client ID: "x509::CN=org1admin, OU=admin, O=Hyperledger, ST=North Carolina, C=US::CN=ca.org1.example.com, O=org1.example.com, L=Durham, ST=North Carolina, C=US";

/*
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String transferenciaJamon(final Context ctx, final String id, final String newOwner, final String newValue) {
        ChaincodeStub stub = ctx.getStub();
        String state = stub.getStringState(id);

        if (state == null || state.isEmpty()) {
            String errorMessage = String.format("Jamon no registrado", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoteSetterErrors.JAMON_NOT_FOUND.toString());
        }

        AssetLote asset = genson.deserialize(state, AssetLote.class);
        List<String> listaIntermediarios = asset.getIntermediarios();
        listaIntermediarios.add(newOwner);

        AssetLote
            newAsset = new AssetLote(asset.getId(), asset.getRaza(), asset.getAlimentacion(), asset.getDenominacionOrigen(), newOwner ,newValue, asset.getIntermediarios());
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(id, sortedJson);

        return "Nuevo propietario: " + newOwner;
    }

 */







}
