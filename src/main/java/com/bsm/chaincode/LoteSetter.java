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
        LOTE_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AssetLote registrarLote(final Context ctx, final String id, final String producto, final String kg, final String origen) {
        //Solo puede registrar la ORG1
        ChaincodeStub stub = ctx.getStub();

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
    public String transportararLote(final Context ctx, final String id, final String km) {
        //Solo puede transportar la ORG2
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
        new AssetLote(asset.getId(),asset.getProduct(),asset.getKg(),km,asset.getPrecioKgFinal(),asset.getOrigen(),asset.getIntermediarios());

        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(id, sortedJson);

        return km + "Km transportados pr la empresa : " /*Añadir nombre peer*/;
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

        return "Lote de " + asset.getProduct() + "en venta a " + precio + "€/kg";
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