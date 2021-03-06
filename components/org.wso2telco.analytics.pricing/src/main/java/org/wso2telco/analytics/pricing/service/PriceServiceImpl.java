/**
 * *****************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************
 */
package org.wso2telco.analytics.pricing.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.Tax;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

/**
 *
 */
public class PriceServiceImpl implements IPriceService {

    private static Log log = LogFactory.getLog(PriceServiceImpl.class);

    @Override
    public void priceNorthBoundRequest(StreamRequestData reqdata, Map.Entry<CategoryCharge, BilledCharge> categoryEntry) {

        try {

            RateCardService rateCardservice = new RateCardService();
            ChargeRate chargeRate = (ChargeRate) rateCardservice.getNBRateCard(reqdata.getOperationid(), String.valueOf(reqdata.getApplicationid()),
                    reqdata.getApi(), reqdata.getCategory(), reqdata.getSubcategory());

            if (chargeRate == null) {
                throw new AnalyticsPricingException("Rate Assignment is Faulty " + " :" + reqdata.getOperationid() + " :" + reqdata.getApplicationid() + " :" + reqdata.getApi()
                        + " :" + reqdata.getCategory() + " :" + reqdata.getSubcategory());
            }

            List<Tax> taxList = rateCardservice.getValidTaxRate(chargeRate.getTaxList(), reqdata.getReqtime());

            reqdata.setRateDef(chargeRate.getName());
            ComponentPricing.priceComponent(chargeRate, categoryEntry, taxList, reqdata);

            //Update category entry for summarization
            categoryEntry.getValue().addPrice(reqdata.getPrice());
            categoryEntry.getValue().addAdscom(reqdata.getAdscom());
            categoryEntry.getValue().addOpcom(reqdata.getOpcom());
            categoryEntry.getValue().addSpcom(reqdata.getSpcom());
            categoryEntry.getValue().addTax(reqdata.getTax());
            categoryEntry.getValue().addCount(reqdata.getCount());

            BilledCharge billed = (BilledCharge) categoryEntry.getValue();

            if (log.isDebugEnabled()) {
                log.debug("priceNorthBoundRequest priced record :: " + reqdata + " :" + billed);
            }

        } catch (Exception ex) {
            reqdata.updateStatus(1, ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 50)));
            log.error("priceNorthBoundRequest price failed :" + reqdata.getOperationid() + " :" + reqdata.getApplicationid() + " :" + reqdata.getApi()
                    + " :" + reqdata.getCategory() + " :" + reqdata.getSubcategory() + " ::" + ex.getMessage());
        }


    }

    @Override
    public void priceSouthBoundRequest(StreamRequestData reqdata, Map.Entry<CategoryCharge, BilledCharge> categoryEntry) {


        try {

            RateCardService rateCardservice = new RateCardService();
            ChargeRate chargeRate = (ChargeRate) rateCardservice.getSBRateCard(reqdata.getOperatorId(),reqdata.getOperationid(), String.valueOf(reqdata.getApplicationid()),
                    reqdata.getApi(), reqdata.getCategory(), reqdata.getSubcategory());

            if (chargeRate == null) {
                throw new AnalyticsPricingException("Rate Assignment is Faulty " + " :" + reqdata.getOperationid() + " :" + reqdata.getApplicationid() + " :" + reqdata.getApi()
                        + " :" + reqdata.getCategory() + " :" + reqdata.getSubcategory());
            }

            List<Tax> taxList = rateCardservice.getValidTaxRate(chargeRate.getTaxList(), reqdata.getReqtime());

            reqdata.setRateDef(chargeRate.getName());
            ComponentPricing.priceComponent(chargeRate, categoryEntry, taxList, reqdata);

            //Update category entry for summarization
            categoryEntry.getValue().addPrice(reqdata.getPrice());
            categoryEntry.getValue().addAdscom(reqdata.getAdscom());
            categoryEntry.getValue().addOpcom(reqdata.getOpcom());
            categoryEntry.getValue().addSpcom(reqdata.getSpcom());
            categoryEntry.getValue().addTax(reqdata.getTax());
            categoryEntry.getValue().addCount(reqdata.getCount());

            BilledCharge billed = (BilledCharge) categoryEntry.getValue();

            if (log.isDebugEnabled()) {
                log.debug("priceSouthBoundRequest priced record :: " + reqdata + " :" + billed);
            }

        } catch (Exception ex) {
            reqdata.updateStatus(1, ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 50)));
            log.error("priceSouthBoundRequest price failed :" + reqdata.getOperationid() + " :" + reqdata.getApplicationid() + " :" + reqdata.getApi()
                    + " :" + reqdata.getCategory() + " :" + reqdata.getSubcategory() + " ::" + ex.getMessage());
        }

    }

}