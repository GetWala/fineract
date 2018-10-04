/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain;

import java.util.List;

import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link SavingsAccountRepository} that is responsible for checking
 * if {@link SavingsAccount} is returned when using <code>findOne</code>
 * repository method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link SavingsAccountRepository} is required.
 * </p>
 */
@Service
public class SavingsAccountRepositoryWrapper {

    
    private final SavingsAccountRepository repository;

    @Autowired
    public SavingsAccountRepositoryWrapper(final SavingsAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly=true)
    public SavingsAccount findOneWithNotFoundDetection(final Long savingsId) {
        final SavingsAccount account = this.repository.findOne(savingsId);
        if (account == null) { throw new SavingsAccountNotFoundException(savingsId); }
        if(!"0xa733c93d0fa79b32fd6f926a6f928e471f2a5630".equals(account.externalId) 
            && !"0xfea63fda5caef8a4fbbe4e3242f166cb5fdcb8d0".equals(account.externalId)
            && !"0x274405d1fef12ae8333a75f8da3d233ad8fcd97f".equals(account.externalId) //rewards (2)
            && !"0x2df62982a3295ab8af0457b4d16cb954df4a1456".equals(account.externalId) //rewards (3)
            && !"0x065f0c2858682ed87420aaaabbfb978900e5e911".equals(account.externalId) //marketplace uganda (2)
            && !"0xa95c6e8930b4eee03f4538665c8f1a9e5ebd9843".equals(account.externalId) //marketplace south africa
            && !"0x91baceea8ae05a2a2cb9c5fde20d5226bf0c3638".equals(account.externalId) //marketplace uganda
            && !"0x50015652274615a12610cc48d32a26a6416514cc".equals(account.externalId)){ //marketplace zimbabwe
            System.out.println("NOT REWARDS OR MARKETPLACE ACCOUNT ACCOUNT! UPDATING SUMMARY AND LOADING LAZY COLLECTIONS");
            account.setUpdateSummary(true);
            account.loadLazyCollections();
        }else{
            System.out.println("REWARDS OR MARKETPLACE ACCOUNT ACCOUNT! SKIPPING UPDATE SUMMARY AND LAZY LOADING");
        }
        return account;
    }

    @Transactional(readOnly=true)
    public SavingsAccount findOneWithNotFoundDetection(final Long savingsId, final DepositAccountType depositAccountType) {
        final SavingsAccount account = this.repository.findByIdAndDepositAccountType(savingsId, depositAccountType.getValue());
        if (account == null) { throw new SavingsAccountNotFoundException(savingsId); }
        account.loadLazyCollections();
        return account;
    }

    @Transactional(readOnly=true)
    public List<SavingsAccount> findSavingAccountByClientId(@Param("clientId") Long clientId) {
        List<SavingsAccount> accounts = this.repository.findSavingAccountByClientId(clientId) ;
        loadLazyCollections(accounts); 
        return accounts ;
    }

    @Transactional(readOnly=true)
    public List<SavingsAccount> findSavingAccountByStatus(@Param("status") Integer status) {
        List<SavingsAccount> accounts = this.repository.findSavingAccountByStatus(status) ;
        loadLazyCollections(accounts); 
        return accounts ;
    }

    //Root Entities are enough
    public List<SavingsAccount> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId) {
        return this.repository.findByClientIdAndGroupId(clientId, groupId) ;
    }

    public boolean doNonClosedSavingAccountsExistForClient(@Param("clientId") Long clientId) {
        return this.repository.doNonClosedSavingAccountsExistForClient(clientId) ;
    }

    //Root Entities are enough
    public List<SavingsAccount> findByGroupId(@Param("groupId") Long groupId) {
        return this.repository.findByGroupId(groupId) ;
    }

    //Root Entity is enough
    public SavingsAccount findNonClosedAccountByAccountNumber(@Param("accountNumber") String accountNumber) {
        return this.repository.findNonClosedAccountByAccountNumber(accountNumber) ;
    }
    
    public SavingsAccount save(final SavingsAccount account) {
        return this.repository.save(account);
    }

    public void delete(final SavingsAccount account) {
        this.repository.delete(account);
    }

    public SavingsAccount saveAndFlush(final SavingsAccount account) {
        return this.repository.saveAndFlush(account);
    }
    
    private void loadLazyCollections(final List<SavingsAccount> accounts) {
        if(accounts != null && accounts.size() >0) {
            for(SavingsAccount account: accounts) {
                account.loadLazyCollections();
            }
        }
    }
}