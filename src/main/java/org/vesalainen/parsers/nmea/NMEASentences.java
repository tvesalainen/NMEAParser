/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.nmea;

import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;

/**
 *
 * @author Timo Vesalainen
 */
public class NMEASentences
{
    @Rule("'AAM'")
    protected void aam(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.AAM);
    }
    @Rule("'ABK'")
    protected void abk(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ABK);
    }
    @Rule("'ACA'")
    protected void aca(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ACA);
    }
    @Rule("'ACS'")
    protected void acs(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ACS);
    }
    @Rule("'AIR'")
    protected void air(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.AIR);
    }
    @Rule("'ALM'")
    protected void alm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ALM);
    }
    @Rule("'ALR'")
    protected void alr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ALR);
    }
    @Rule("'APA'")
    protected void apa(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.APA);
    }
    @Rule("'APB'")
    protected void apb(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.APB);
    }
    @Rule("'BEC'")
    protected void bec(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BEC);
    }
    @Rule("'BOD'")
    protected void bod(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BOD);
    }
    @Rule("'BWC'")
    protected void bwc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BWC);
    }
    @Rule("'BWR'")
    protected void bwr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BWR);
    }
    @Rule("'BWW'")
    protected void bww(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BWW);
    }
    @Rule("'CUR'")
    protected void cur(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.CUR);
    }
    @Rule("'DBK'")
    protected void dbk(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DBK);
    }
    @Rule("'DBS'")
    protected void dbs(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DBS);
    }
    @Rule("'DBT'")
    protected void dbt(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DBT);
    }
    @Rule("'DCN'")
    protected void dcn(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DCN);
    }
    @Rule("'DPT'")
    protected void dpt(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DPT);
    }
    @Rule("'DSC'")
    protected void dsc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DSC);
    }
    @Rule("'DSE'")
    protected void dse(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DSE);
    }
    @Rule("'DSI'")
    protected void dsi(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DSI);
    }
    @Rule("'DSR'")
    protected void dsr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DSR);
    }
    @Rule("'DTM'")
    protected void dtm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.DTM);
    }
    @Rule("'FSI'")
    protected void fsi(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.FSI);
    }
    @Rule("'GBS'")
    protected void gbs(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GBS);
    }
    @Rule("'GGA'")
    protected void gga(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GGA);
    }
    @Rule("'GLC'")
    protected void glc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GLC);
    }
    @Rule("'GLL'")
    protected void gll(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GLL);
    }
    @Rule("'GMP'")
    protected void gmp(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GMP);
    }
    @Rule("'GNS'")
    protected void gns(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GNS);
    }
    @Rule("'GRS'")
    protected void grs(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GRS);
    }
    @Rule("'GSA'")
    protected void gsa(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GSA);
    }
    @Rule("'GST'")
    protected void gst(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GST);
    }
    @Rule("'GSV'")
    protected void gsv(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GSV);
    }
    @Rule("'GTD'")
    protected void gtd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GTD);
    }
    @Rule("'GXA'")
    protected void gxa(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.GXA);
    }
    @Rule("'HDG'")
    protected void hdg(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HDG);
    }
    @Rule("'HDM'")
    protected void hdm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HDM);
    }
    @Rule("'HDT'")
    protected void hdt(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HDT);
    }
    @Rule("'HFB'")
    protected void hfb(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HFB);
    }
    @Rule("'HMR'")
    protected void hmr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HMR);
    }
    @Rule("'HMS'")
    protected void hms(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HMS);
    }
    @Rule("'HSC'")
    protected void hsc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HSC);
    }
    @Rule("'HTC'")
    protected void htc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HTC);
    }
    @Rule("'HTD'")
    protected void htd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.HTD);
    }
    @Rule("'ITS'")
    protected void its(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ITS);
    }
    @Rule("'LCD'")
    protected void lcd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LCD);
    }
    @Rule("'LRF'")
    protected void lrf(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LRF);
    }
    @Rule("'LTI'")
    protected void lti(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LTI);
    }
    @Rule("'LR1'")
    protected void lr1(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LR1);
    }
    @Rule("'LR2'")
    protected void lr2(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LR2);
    }
    @Rule("'LR3'")
    protected void lr3(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.LR3);
    }
    @Rule("'MLA'")
    protected void mla(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MLA);
    }
    @Rule("'MSK'")
    protected void msk(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MSK);
    }
    @Rule("'MSS'")
    protected void mss(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MSS);
    }
    @Rule("'MTW'")
    protected void mtw(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MTW);
    }
    @Rule("'MWD'")
    protected void mwd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MWD);
    }
    @Rule("'MWV'")
    protected void mwv(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.MWV);
    }
    @Rule("'OLN'")
    protected void oln(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.OLN);
    }
    @Rule("'OSD'")
    protected void osd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.OSD);
    }
    @Rule("'R00'")
    protected void r00(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.R00);
    }
    @Rule("'RMA'")
    protected void rma(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RMA);
    }
    @Rule("'RMB'")
    protected void rmb(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RMB);
    }
    @Rule("'RMC'")
    protected void rmc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RMC);
    }
    @Rule("'ROT'")
    protected void rot(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ROT);
    }
    @Rule("'RPM'")
    protected void rpm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RPM);
    }
    @Rule("'RSA'")
    protected void rsa(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RSA);
    }
    @Rule("'RSD'")
    protected void rsd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RSD);
    }
    @Rule("'RTE'")
    protected void rte(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.RTE);
    }
    @Rule("'SFI'")
    protected void sfi(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.SFI);
    }
    @Rule("'SSD'")
    protected void ssd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.SSD);
    }
    @Rule("'STN'")
    protected void stn(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.STN);
    }
    @Rule("'TDS'")
    protected void tds(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TDS);
    }
    @Rule("'TFI'")
    protected void tfi(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TFI);
    }
    @Rule("'TLB'")
    protected void tlb(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TLB);
    }
    @Rule("'TPC'")
    protected void tpc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TPC);
    }
    @Rule("'TPR'")
    protected void tpr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TPR);
    }
    @Rule("'TPT'")
    protected void tpt(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TPT);
    }
    @Rule("'TLL'")
    protected void tll(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TLL);
    }
    @Rule("'TRF'")
    protected void trf(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TRF);
    }
    @Rule("'TTM'")
    protected void ttm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TTM);
    }
    @Rule("'TUT'")
    protected void tut(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TUT);
    }
    @Rule("'TXT'")
    protected void txt(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.TXT);
    }
    @Rule("'VBW'")
    protected void vbw(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VBW);
    }
    @Rule("'VDR'")
    protected void vdr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VDR);
    }
    @Rule("'VHW'")
    protected void vhw(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VHW);
    }
    @Rule("'VLW'")
    protected void vlw(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VLW);
    }
    @Rule("'VPW'")
    protected void vpw(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VPW);
    }
    @Rule("'VSD'")
    protected void vsd(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VSD);
    }
    @Rule("'VTG'")
    protected void vtg(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VTG);
    }
    @Rule("'VWR'")
    protected void vwr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VWR);
    }
    @Rule("'WCV'")
    protected void wcv(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.WCV);
    }
    @Rule("'WNC'")
    protected void wnc(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.WNC);
    }
    @Rule("'WPL'")
    protected void wpl(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.WPL);
    }
    @Rule("'XDR'")
    protected void xdr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.XDR);
    }
    @Rule("'XTE'")
    protected void xte(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.XTE);
    }
    @Rule("'XTR'")
    protected void xtr(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.XTR);
    }
    @Rule("'ZDA'")
    protected void zda(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ZDA);
    }
    @Rule("'ZDL'")
    protected void zdl(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ZDL);
    }
    @Rule("'ZFO'")
    protected void zfo(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ZFO);
    }
    @Rule("'ZTG'")
    protected void ztg(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ZTG);
    }
    @Rule("'ABM'")
    protected void abm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.ABM);
    }
    @Rule("'BBM'")
    protected void bbm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.BBM);
    }
    @Rule("'VDM'")
    protected void vdm(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VDM);
    }
    @Rule("'VDO'")
    protected void vdo(@ParserContext("data") NMEAObserver data)
    {
        data.setMessageType(MessageType.VDO);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            for (MessageType mt : MessageType.values())
            {
                System.err.println("    @Rule(\"'"+mt.name()+"'\")");
                System.err.println("    protected void "+mt.name().toLowerCase()+"(@ParserContext(\"data\") NMEAObserver data)");
                System.err.println("    {");
                System.err.println("        data.setMessageType(MessageType."+mt.name()+");");
                System.err.println("    }");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
