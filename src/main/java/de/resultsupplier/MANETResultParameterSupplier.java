package de.resultsupplier;

import java.util.function.Supplier;

import de.results.MANETResultParameter;

public class MANETResultParameterSupplier implements Supplier<MANETResultParameter> {

    public MANETResultParameterSupplier() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public MANETResultParameter get() {
	// TODO Auto-generated method stub
	return new MANETResultParameter();
    }

}
