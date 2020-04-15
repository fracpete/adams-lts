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
 * WekaAggregateEvaluations.java
 * Copyright (C) 2012-2020 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.base.BaseObject;
import adams.core.base.BaseString;
import adams.flow.container.WekaEvaluationContainer;
import adams.flow.core.Token;
import weka.classifiers.AggregateEvaluations;
import weka.classifiers.Evaluation;

import java.util.Arrays;
import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * Aggregates incoming weka.classifiers.Evaluation objects and forwards the current aggregated state.<br>
 * Only works with the predictions stored in the evaluation object.<br>
 * NB: Relative absolute error and Root relative squared error will differ a bit, due to change in priors.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;weka.classifiers.Evaluation<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.WekaEvaluationContainer<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.WekaEvaluationContainer<br>
 * <br><br>
 * Container information:<br>
 * - adams.flow.container.WekaEvaluationContainer: Evaluation, Model, Prediction output, Original indices, Test data
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: WekaAggregateEvaluations
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-class-label &lt;adams.core.base.BaseString&gt; [-class-label ...] (property: classLabels)
 * &nbsp;&nbsp;&nbsp;The class labels to use, if none provided the labels from the first Evaluation
 * &nbsp;&nbsp;&nbsp;object's header will get used.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WekaAggregateEvaluations
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = 3799673803958040769L;

  /** the key for storing the current accumulated error in the backup. */
  public final static String BACKUP_EVALUATION = "evaluation";

  /** the class labels to use. */
  protected BaseString[] m_ClassLabels;

  /** the current evaluation state. */
  protected AggregateEvaluations m_Evaluation;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Aggregates incoming " + Evaluation.class.getName() + " objects "
	+ "and forwards the current aggregated state.\n"
	+ "Only works with the predictions stored in the evaluation object.\n"
	+ "NB: Relative absolute error and Root relative squared error will differ a bit, due to change in priors.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "class-label", "classLabels",
      new BaseString[0]);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Evaluation = null;
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_EVALUATION);
  }

  /**
   * Backs up the current state of the actor before update the variables.
   *
   * @return		the backup
   */
  @Override
  protected Hashtable<String,Object> backupState() {
    Hashtable<String,Object>	result;

    result = super.backupState();

    result.put(BACKUP_EVALUATION, m_Evaluation);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_EVALUATION)) {
      m_Evaluation = (AggregateEvaluations) state.get(BACKUP_EVALUATION);
      state.remove(BACKUP_EVALUATION);
    }

    super.restoreState(state);
  }

  /**
   * Sets the class labels to use to override the ones from the incoming Evaluation objects.
   *
   * @param value	the labels
   */
  public void setClassLabels(BaseString[] value) {
    m_ClassLabels = value;
    reset();
  }

  /**
   * Returns the currently set class labels to override the ones from the incoming Evaluation objects.
   *
   * @return		the labels
   */
  public BaseString[] getClassLabels() {
    return m_ClassLabels;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String classLabelsTipText() {
    return "The class labels to use, if none provided the labels from the first Evaluation object's header will get used.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Evaluation.class, WekaEvaluationContainer.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{WekaEvaluationContainer.class};
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "classLabels", m_ClassLabels, "labels override: ");
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    Evaluation	input;
    Evaluation  agg;

    result = null;
    if (m_InputToken.getPayload() instanceof WekaEvaluationContainer)
      input = (Evaluation) ((WekaEvaluationContainer) m_InputToken.getPayload()).getValue(WekaEvaluationContainer.VALUE_EVALUATION);
    else
      input = (Evaluation) m_InputToken.getPayload();

    try {
      if (m_Evaluation == null) {
        m_Evaluation = new AggregateEvaluations();
        if (m_ClassLabels.length > 0)
          m_Evaluation.setClassLabels(Arrays.asList(BaseObject.toStringArray(m_ClassLabels)));
      }
      m_Evaluation.add(input);
      agg = m_Evaluation.aggregated();
      if (agg == null) {
        if (m_Evaluation.hasLastError())
          result = m_Evaluation.getLastError();
        else
          result = "Failed to aggregate predictions!";
      }
      if (agg != null)
        m_OutputToken = new Token(agg);
    }
    catch (Exception e) {
      result = handleException("Failed to aggregate evaluation!", e);
    }
    
    return result;
  }
}
