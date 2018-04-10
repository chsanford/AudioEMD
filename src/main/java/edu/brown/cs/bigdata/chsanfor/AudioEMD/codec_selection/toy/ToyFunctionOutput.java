package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.toy;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Created by Clayton on 3/15/18.
 */
public class ToyFunctionOutput implements FunctionOutput {
    double value;

    public ToyFunctionOutput(double value) {
        this.value = value;
    }


    @Override
    public void delete() {
        ;
    }
}
