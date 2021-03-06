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
 *    PCANNSearch.java
 *    Copyright (C) 1999-2007 University of Waikato
 */

package weka.core.neighboursearch;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.PLSFilter;
import weka.filters.unsupervised.attribute.PrincipalComponentsJ;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Class implementing the brute force search algorithm for nearest neighbour search, filtered using PLS.
 * <br><br>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre> -S
 *  Skip identical instances (distances equal to zero).
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author Dale
 * @version $Revision$
 */
public class PCANNSearch
  extends NewNNSearch {

  private static final long serialVersionUID = 1915484723703917241L;
  /** The neighbourhood of instances to find neighbours in. */
  protected PrincipalComponentsJ m_pca=null;
  /** the amount of varaince to cover in the original data when
  retaining the best n PC's. */
  protected double m_CoverVariance = 0.95;

  /** maximum number of attributes in the transformed attribute name. */
  protected int m_MaxAttrsInName = 5;
  protected Instances m_myInstances;
  /** the type of preprocessing */
  protected int m_Preprocessing = PLSFilter.PREPROCESSING_CENTER;
  /**
   * Constructor. Needs setInstances(Instances) 
   * to be called before the class is usable.
   */
  public PCANNSearch() {
    super();
  }
  /**
   * Sets the amount of variance to account for when retaining
   * principal components.
   * 
   * @param value 	the proportion of total variance to account for
   */
  public void setVarianceCovered(double value) {
    m_CoverVariance = value;
  }

  /**
   * Gets the proportion of total variance to account for when
   * retaining principal components.
   * 
   * @return 		the proportion of variance to account for
   */
  public double getVarianceCovered() {
    return m_CoverVariance;
  }

  /**
   * Sets maximum number of attributes to include in
   * transformed attribute names.
   * 
   * @param value 	the maximum number of attributes
   */
  public void setMaximumAttributeNames(int value) {
    m_MaxAttrsInName = value;
  }

  /**
   * Gets maximum number of attributes to include in
   * transformed attribute names.
   * 
   * @return 		the maximum number of attributes
   */
  public int getMaximumAttributeNames() {
    return m_MaxAttrsInName;
  }
  protected Instances transformInstances(Instances in) throws Exception{ 
    m_pca=buildFilter(m_CoverVariance,m_MaxAttrsInName);
    //m_pca.setInputFormat(in);
    Instances ret=null;
    m_pca.setInputFormat(in);  // filter capabilities are checked here
    ret = Filter.useFilter(in, m_pca);
    
/*    for (int i=0;i<in.numInstances();i++) {
      Instance inst=in.instance(i);
      m_pca.input(inst);
    }
    m_pca.batchFinished();
    Instances filteredData = Filter.useFilter(in,m_pca );
    
    Instances ret=new Instances(filteredData,0);
    for (int i=0;i<in.numInstances();i++) {
      Instance inst=in.instance(i);
      m_pca.input(inst);
      m_pca.batchFinished();        
      ret.add(m_pca.output());
    }*/
    return(ret);   
  }
  
  protected Instance transformInstance(Instance in) throws Exception { 
    m_pca.input(in);
    m_pca.batchFinished();     
    return(m_pca.output());    
  }
  
  /**
   * Constructor that uses the supplied set of 
   * instances.
   * 
   * @param insts	the instances to use
   */
  public PCANNSearch(Instances insts) {
    super(insts);
    try {
      m_DistanceFunction.setInstances(transformInstances(insts));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected PrincipalComponentsJ buildFilter(double cv,int maxatt) {
    PrincipalComponentsJ pcafilter = new PrincipalComponentsJ();
    pcafilter.setMaximumAttributeNames(maxatt);
    pcafilter.setVarianceCovered(cv);
    return(pcafilter);
  }
  
  
  /**
   * Returns k nearest instances in the current neighbourhood to the supplied
   * instance.
   *  
   * @param target 	The instance to find the k nearest neighbours for.
   * @param kNN		The number of nearest neighbours to find.
   * @return		the k nearest neighbors
   * @throws Exception  if the neighbours could not be found.
   */
  @Override
  public Instances kNearestNeighbours(Instance target, int kNN) throws Exception {
  
    if(m_Stats!=null)
      m_Stats.searchStart();
    m_neighbours.clear();
    double distance; 
    double last_distance=Double.POSITIVE_INFINITY;
    Instance t_instance=transformInstance(target);
    for(int i=0; i<m_Instances.numInstances(); i++) {
      if(target == m_Instances.instance(i)) {//for hold-one-out cross-validation
        continue;
      }
      if(m_Stats!=null) {
        m_Stats.incrPointCount();
      }
      distance = m_DistanceFunction.distance(t_instance, m_myInstances.instance(i), last_distance, m_Stats);
      if(distance == 0.0 && m_SkipIdentical) {
	continue;
      }
      if (distance < last_distance) {
	this.m_neighbours.add(new InstanceNode(i,distance));
	if (m_neighbours.size() > kNN) {
	  m_neighbours.remove(m_neighbours.size()-1);
	  last_distance=m_neighbours.get(m_neighbours.size() - 1).distance;
	}
      }      
    }
    Instances neighbours = new Instances(m_Instances,m_neighbours.size() );   
    int index=0;
    m_Distances = new double[m_neighbours.size()];
    
    Iterator<InstanceNode> iter = m_neighbours.iterator();
    while(iter.hasNext()) {
      InstanceNode in=iter.next();
      m_Distances[index++]=in.distance;
      //System.err.print(in.distance+" ");
      neighbours.add(m_Instances.instance(in.instance_index));
    }
    
    
    m_DistanceFunction.postProcessDistances(m_Distances);
    if(m_Stats!=null)
      m_Stats.searchFinish();
    
    return neighbours;    
  }
  
  
  /** 
   * Sets the instances comprising the current neighbourhood.
   * 
   * @param insts 	The set of instances on which the nearest neighbour 
   * 			search is carried out. Usually this set is the 
   * 			training set. 
   * @throws Exception	if setting of instances fails
   */
  @Override
  public void setInstances(Instances insts) throws Exception {
    m_Instances=insts;
    Instances t_instances=this.transformInstances(insts);
    m_myInstances= t_instances;
    m_DistanceFunction.setInstances(t_instances);
  }
  
  /** 
   * Updates the LinearNNSearch to cater for the new added instance. This 
   * implementation only updates the ranges of the DistanceFunction class, 
   * since our set of instances is passed by reference and should already have 
   * the newly added instance.
   * 
   * @param ins 	The instance to add. Usually this is the instance that 
   * 			is added to our neighbourhood i.e. the training 
   * 			instances.
   * @throws Exception	if the given instances are null
   */
  @Override
  public void update(Instance ins) throws Exception {
    if(m_Instances==null)
      throw new Exception("No instances supplied yet. Cannot update without"+
                          "supplying a set of instances first.");
    m_DistanceFunction.update(this.transformInstance(ins));
  }
  
  /** 
   * Adds the given instance info. This implementation updates the range
   * datastructures of the DistanceFunction class.
   * 
   * @param ins 	The instance to add the information of. Usually this is
   * 			the test instance supplied to update the range of 
   * 			attributes in the  distance function.
   */
  @Override
  public void addInstanceInfo(Instance ins) {
    if(m_Instances!=null)
      try{ update(ins); }
      catch(Exception ex) { ex.printStackTrace(); }
  }
  
  @Override
  public Enumeration listOptions() {
    Vector result = new Vector();
    
    result.addElement(new Option(
	"\tRetain enough PC attributes to account\n"
	+"\tfor this proportion of variance in the original data.\n"
	+ "\t(default: 0.95)",
	"R", 1, "-R <num>"));

    result.addElement(new Option(
	"\tMaximum number of attributes to include in \n"
	+ "\ttransformed attribute names.\n"
	+ "\t(-1 = include all, default: 5)", 
	"A", 1, "-A <num>"));
    
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements())
      result.addElement(en.nextElement());

    return result.elements();
  }
  
  /**
  * Sets the type of preprocessing to use 
  *
  * @param value 	the preprocessing type
  */
 public void setPreprocessing(SelectedTag value) {
   if (value.getTags() == PLSFilter.TAGS_PREPROCESSING) {
     m_Preprocessing = value.getSelectedTag().getID();
   }
 }
  @Override
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;

    tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0)
      setVarianceCovered(Double.parseDouble(tmpStr));
    else
      setVarianceCovered(0.95);

    tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0)
      setMaximumAttributeNames(Integer.parseInt(tmpStr));
    else
      setMaximumAttributeNames(5);
    super.setOptions(options);
  }

  
  @Override
  public String[] getOptions() {
    Vector<String>	result;
    String[]		options;
    int			i;
    
    result = new Vector<String>();
    result.add("-R");
    result.add("" + getVarianceCovered());

    result.add("-A");
    result.add("" + getMaximumAttributeNames());
    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);
    
    return result.toArray(new String[result.size()]);
  }
}
