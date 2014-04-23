/*
 * Copyright (C) 2013 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.parsers.nmea.ais;

/**
 *
 * @author Timo Vesalainen
 */
public enum SensorTypes
{

    /**
     * No data (default)
     */
    NoDataDefault("No data (default)"),
    /**
     * Raw real time
     */
    RawRealTime("Raw real time"),
    /**
     * Real time with quality control
     */
    RealTimeWithQualityControl("Real time with quality control"),
    /**
     * Predicted (based on historical statistics)
     */
    PredictedBasedOnHistoricalStatistics("Predicted (based on historical statistics)"),
    /**
     * Forecast (predicted, refined with real-time information)
     */
    ForecastPredictedRefinedWithRealTimeInformation("Forecast (predicted, refined with real-time information)"),
    /**
     * Nowcast (a continuous forecast)
     */
    NowcastAContinuousForecast("Nowcast (a continuous forecast)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse("(reserved for future use)"),
    /**
     * Sensor not available
     */
    SensorNotAvailable("Sensor not available");
    private String description;

    SensorTypes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
