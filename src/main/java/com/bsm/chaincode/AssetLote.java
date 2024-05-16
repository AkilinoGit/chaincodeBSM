package com.bsm.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;
import java.util.Objects;

@DataType()
public final class AssetLote {

    @Property()
    private final String id;

    @Property()
    private final String producto;

    @Property()
    private final String kg;

    @Property()
    private final String km;

    @Property()
    private final String precioKgFinal;

    @Property()
    private final String origen;

    @Property()
    private final List<String> intermediarios;


    public AssetLote(@JsonProperty("id") final String id,
                      @JsonProperty("producto") final String producto,
                      @JsonProperty("kg") final String kg,
                      @JsonProperty("km") final String km,
                      @JsonProperty("precioKg") final String precioKg,
                      @JsonProperty("origen") final String origen,
                      @JsonProperty("intermediarios") final List<String> intermediarios) {
        this.id = id;
        this.producto = producto;
        this.kg = kg;
        this.km = km;
        this.precioKgFinal = precioKg;
        this.origen = origen;
        this.intermediarios = intermediarios;
    }


    public String getId() {
        return id;
    }

    public String getProduct() {
        return producto;
    }

    public String getKg() {
        return kg;
    }

    public String getKm() {
        return km;
    }

    public String getPrecioKgFinal() {
        return precioKgFinal;
    }

    public String getOrigen() {
        return origen;
    }

    public List<String> getIntermediarios() {
        return intermediarios;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        AssetLote other = (AssetLote) obj;

        return Objects.deepEquals(
                new String[] {getId(), getProduct(), getKg(), getKm(), getPrecioKgFinal(), getOrigen(), getIntermediarios().toString()},
                new String[] {other.getId(), other.getProduct(), other.getKg(), other.getKm(), other.getPrecioKgFinal(), other.getOrigen(), other.getIntermediarios().toString()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProduct(), getKg(), getKm(), getPrecioKgFinal(), getOrigen(), getIntermediarios().toString());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [id=" + id + ", producto="
                + producto + ", kg=" + kg + ", km =" + km + ", precioKgFinal=" + precioKgFinal + ", origen=" + origen + ", intermediarios=" + intermediarios + "]";
    }
}
