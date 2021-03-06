/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PeakTransformed.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.meta;

import java.util.ArrayList;
import java.util.Enumeration;

import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.WeightedInstancesHandler;

/**
 <!-- globalinfo-start -->
 * Uses the maximum peak in the instances.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.rules.ZeroR)</pre>
 *
 * <pre>
 * Options specific to classifier weka.classifiers.rules.ZeroR:
 * </pre>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 <!-- options-end -->
 *
 * @author Dale
 * @version $Revision$
 */
public class PeakTransformed
  extends SingleClassifierEnhancer
  implements WeightedInstancesHandler{

  /** for serialization. */
  private static final long serialVersionUID = 2483812207638252172L;

  /** the header information of the transformed data. */
  protected Instances m_Header = null;

  /**
   * Returns a string describing classifier.
   *
   * @return 		a description suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Uses the maximum peak in the instances.";
  }

  /**
   * Just finds the maximum peak.
   *
   * @param in		the instance to analyze
   * @return		the new instance
   */
  protected Instance transformInstance(Instance in) {
    double[] values=new double[2];
    Enumeration enu = in.enumerateAttributes();
    double max = Double.NEGATIVE_INFINITY;
    while (enu.hasMoreElements()) {
      Attribute att = (Attribute) enu.nextElement();
      if (att != in.classAttribute()) {
	if (in.value(att) > max) {
	  max=in.value(att);
	}
      }
    }
    values[0]=max;
    values[1]=in.classValue();
    return(new DenseInstance(1.0,values));
  }

  /**
   * Builds the classifier.
   *
   * @param ins		the training data
   * @throws Exception	if training fails
   */
  public void buildClassifier(Instances ins) throws Exception {
    getCapabilities().testWithFail(ins);
    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    atts.add(new Attribute("Peak"));
    atts.add(new Attribute("Class"));
    Instances m_Data = new Instances("SumData",atts,0);

    for (int i=0;i<ins.numInstances();i++) {
      Instance inst=ins.instance(i);
      m_Data.add(transformInstance(inst));
    }
    m_Data.setClassIndex(1);
    m_Header=new Instances(m_Data,0);
    m_Classifier.buildClassifier(m_Data);

  }

  /**
   * Returns the prediction.
   *
   * @param inst	the instance to predict
   * @return		the prediction
   * @throws Exception	if prediction fails
   */
  public double classifyInstance(Instance inst) throws Exception {
    Instance i=transformInstance(inst);
    i.setDataset(m_Header);
    return(m_Classifier.classifyInstance(i));
  }

  /**
   * Returns description of classifier.
   *
   * @return		the model
   */
  public String toString() {
    return m_Classifier.toString();
  }

  public String getRevision() {
    return RevisionUtils.extract("$Revision$");
  }

  /**
   * Main method for running this class.
   *
   * @param argv the options
   */
  public static void main(String[] argv) {
    runClassifier(new PeakTransformed(), argv);
  }
}
