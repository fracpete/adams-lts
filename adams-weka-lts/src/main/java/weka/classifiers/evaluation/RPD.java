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

/**
 * SDR.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package weka.classifiers.evaluation;

import adams.data.statistics.StatUtils;
import weka.core.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the RPD (Ratio of Performance to Deviation) for regression models:
 * RPD = SD / RMSE
 * <br>
 * https://www.academia.edu/4303409/Why_you_dont_need_to_use_RPD
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RPD
  extends AbstractSimpleRegressionMeasure {

  private static final long serialVersionUID = -2991979775999208848L;

  public static final String NAME = "Ratio of Performance to Deviation";

  /**
   * Get the name of this metric
   *
   * @return the name of this metric
   */
  @Override
  public String getMetricName() {
    return NAME;
  }

  /**
   * Get a short description of this metric (algorithm, formulas etc.).
   *
   * @return a short description of this metric
   */
  @Override
  public String getMetricDescription() {
    return NAME + " = SD / RMSE";
  }

  /**
   * Get a list of the names of the statistics that this metrics computes. E.g.
   * an information theoretic evaluation measure might compute total number of
   * bits as well as average bits/instance
   *
   * @return the names of the statistics that this metric computes
   */
  @Override
  public List<String> getStatisticNames() {
    return Arrays.asList(NAME);
  }

  /**
   * Get the value of the named statistic
   *
   * @param statName the name of the statistic to compute the value for
   * @return the computed statistic or Utils.missingValue() if the statistic
   *         can't be computed for some reason
   */
  @Override
  public double getStatistic(String statName) {
    double 	rmse;
    double 	sd;

    if (statName.equals(NAME)) {
      if (m_Actual.size() == 0) {
	return Utils.missingValue();
      }
      else {
	rmse = StatUtils.rmse(m_Actual.toArray(), m_Predicted.toArray());
	sd   = StatUtils.stddev(m_Predicted.toArray(), true);
	return sd / rmse;
      }
    }
    else {
      return Utils.missingValue();
    }
  }

  /**
   * Return a formatted string (suitable for displaying in console or GUI
   * output) containing all the statistics that this metric computes.
   *
   * @return a formatted string containing all the computed statistics
   */
  @Override
  public String toSummaryString() {
    return Utils.padRight(NAME, 41) + Utils.doubleToString(getStatistic(NAME), 4) + "\n";
  }
}
