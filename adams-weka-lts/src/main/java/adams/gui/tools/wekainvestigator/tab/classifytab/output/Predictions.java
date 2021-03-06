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
 * Predictions.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.wekainvestigator.tab.classifytab.output;

import adams.core.MessageCollection;
import adams.data.spreadsheet.SpreadSheet;
import adams.gui.core.MultiPagePane;
import adams.gui.core.SpreadSheetTable;
import adams.gui.tools.wekainvestigator.output.TableContentPanel;
import adams.gui.tools.wekainvestigator.tab.classifytab.PredictionHelper;
import adams.gui.tools.wekainvestigator.tab.classifytab.ResultItem;
import com.github.fracpete.javautils.enumerate.Enumerated;
import weka.classifiers.Evaluation;

import javax.swing.JComponent;

import static com.github.fracpete.javautils.Enumerate.enumerate;

/**
 * Displays the predictions.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Predictions
  extends AbstractOutputGenerator {

  private static final long serialVersionUID = -6829245659118360739L;

  /** whether to prefix the labels with a 1-based index (only nominal classes). */
  protected boolean m_AddLabelIndex;

  /** whether to add an error colunm. */
  protected boolean m_ShowError;

  /** whether to output the probability of the prediction (only nominal classes). */
  protected boolean m_ShowProbability;

  /** whether to output the class distribution (only nominal classes). */
  protected boolean m_ShowDistribution;

  /** whether to output the weight as well. */
  protected boolean m_ShowWeight;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates classifier errors plot.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "add-index", "addLabelIndex",
      false);

    m_OptionManager.add(
      "error", "showError",
      false);

    m_OptionManager.add(
      "probability", "showProbability",
      false);

    m_OptionManager.add(
      "distribution", "showDistribution",
      false);

    m_OptionManager.add(
      "weight", "showWeight",
      false);
  }

  /**
   * Sets whether to prefix the labels with the index.
   *
   * @param value	true if the label is prefixed with the index
   */
  public void setAddLabelIndex(boolean value) {
    m_AddLabelIndex = value;
    reset();
  }

  /**
   * Returns whether to show the error as well.
   *
   * @return		true if the label is prefixed with the index
   */
  public boolean getAddLabelIndex() {
    return m_AddLabelIndex;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addLabelIndexTipText() {
    return "If set to true, then the label is prefixed with the index.";
  }

  /**
   * Sets whether to show the error as well.
   *
   * @param value	true if the error is to be displayed as well
   */
  public void setShowError(boolean value) {
    m_ShowError = value;
    reset();
  }

  /**
   * Returns whether to show the error as well.
   *
   * @return		true if the error is displayed as well
   */
  public boolean getShowError() {
    return m_ShowError;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showErrorTipText() {
    return "If set to true, then the error will be displayed as well.";
  }

  /**
   * Sets whether to show the probability of the prediction as well.
   *
   * @param value	true if the probability is to be displayed as well
   */
  public void setShowProbability(boolean value) {
    m_ShowProbability = value;
    reset();
  }

  /**
   * Returns whether to show the probability as well.
   *
   * @return		true if the probability is displayed as well
   */
  public boolean getShowProbability() {
    return m_ShowProbability;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showProbabilityTipText() {
    return
        "If set to true, then the probability of the prediction will be "
      + "displayed as well (only for nominal class attributes).";
  }

  /**
   * Sets whether to show the class distribution as well.
   *
   * @param value	true if the class distribution is to be displayed as well
   */
  public void setShowDistribution(boolean value) {
    m_ShowDistribution = value;
    reset();
  }

  /**
   * Returns whether to show the class distribution as well.
   *
   * @return		true if the class distribution is displayed as well
   */
  public boolean getShowDistribution() {
    return m_ShowDistribution;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showDistributionTipText() {
    return
        "If set to true, then the class distribution will be displayed as "
      + "well (only for nominal class attributes).";
  }

  /**
   * Sets whether to show the weight as well.
   *
   * @param value	true if the weight is to be displayed as well
   */
  public void setShowWeight(boolean value) {
    m_ShowWeight = value;
    reset();
  }

  /**
   * Returns whether to show the weight as well.
   *
   * @return		true if the weight is displayed as well
   */
  public boolean getShowWeight() {
    return m_ShowWeight;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showWeightTipText() {
    return
        "If set to true, then the instance weight will be displayed as well.";
  }

  /**
   * The title to use for the tab.
   *
   * @return		the title
   */
  public String getTitle() {
    return "Predictions";
  }

  /**
   * Checks whether output can be generated from this item.
   *
   * @param item	the item to check
   * @return		true if output can be generated
   */
  public boolean canGenerateOutput(ResultItem item) {
    return item.hasEvaluation() && (item.getEvaluation().predictions() != null);
  }

  /**
   * Generates the output from the evaluation.
   *
   * @param eval		the evaluation to use
   * @param originalIndices	the original indices, can be null
   * @param additionalAttributes	the additional attributes to use, can be null
   * @param errors		for collecting error messages
   * @return			the generated output
   */
  protected TableContentPanel createOutput(Evaluation eval, int[] originalIndices, SpreadSheet additionalAttributes, MessageCollection errors) {
    SpreadSheet		sheet;
    SpreadSheetTable	table;

    sheet = PredictionHelper.toSpreadSheet(
      this, errors, eval, originalIndices, additionalAttributes, m_AddLabelIndex, m_ShowDistribution, m_ShowProbability, m_ShowError, m_ShowWeight);
    if (sheet == null) {
      if (errors.isEmpty())
	errors.add("Failed to generate prediction!");
      return null;
    }
    table = new SpreadSheetTable(sheet);

    return new TableContentPanel(table, true, true);
  }

  /**
   * Generates output from the item.
   *
   * @param item	the item to generate output for
   * @param errors	for collecting error messages
   * @return		the output component, null if failed to generate
   */
  public JComponent createOutput(ResultItem item, MessageCollection errors) {
    MultiPagePane multiPage;

    if (item.hasFoldEvaluations()) {
      multiPage = newMultiPagePane(item);
      addPage(multiPage, "Full", createOutput(item.getEvaluation(), item.getOriginalIndices(), item.getAdditionalAttributes(), errors), 0);
      for (Enumerated<Evaluation> eval: enumerate(item.getFoldEvaluations()))
	addPage(multiPage, "Fold " + (eval.index + 1), createOutput(item.getFoldEvaluations()[eval.index], null, null, errors), eval.index + 1);
      if (multiPage.getPageCount() > 0)
	multiPage.setSelectedIndex(0);
      return multiPage;
    }
    else {
      return createOutput(item.getEvaluation(), item.getOriginalIndices(), item.getAdditionalAttributes(), errors);
    }
  }
}
