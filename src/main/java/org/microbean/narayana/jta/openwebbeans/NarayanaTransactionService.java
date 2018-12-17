/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2018 microBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.narayana.jta.openwebbeans;

import javax.enterprise.event.TransactionPhase;

import javax.enterprise.inject.Instance;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.ObserverMethod;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;

import org.apache.webbeans.ee.event.TransactionalEventNotifier;

import org.apache.webbeans.spi.TransactionService;

public class NarayanaTransactionService implements TransactionService {

  public NarayanaTransactionService() {
    super();
  }

  @Override
  public final TransactionManager getTransactionManager() {
    Instance<TransactionManager> transactionManagerInstance = CDI.current().select(TransactionManager.class);
    assert transactionManagerInstance != null;
    final TransactionManager transactionManager;
    if (transactionManagerInstance.isUnsatisfied()) {
      transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
    } else {
      transactionManager = transactionManagerInstance.get();
    }
    return transactionManager;
  }

  @Override
  public final Transaction getTransaction() {
    final Instance<Transaction> transactionInstance = CDI.current().select(Transaction.class);
    Transaction transaction = null;
    if (transactionInstance.isUnsatisfied()) {
      final TransactionManager transactionManager = this.getTransactionManager();
      if (transactionManager != null) {
        try {
          transaction = transactionManager.getTransaction();
        } catch (final SystemException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    } else {
      transaction = transactionInstance.get();
    }
    return transaction;
  }

  /**
   * Returns the {@link UserTransaction} present in this environment
   * by invoking the {@link
   * com.arjuna.ats.jta.UserTransaction#userTransaction()} method and
   * returning its result.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The return value of this method is used as the backing
   * implementation of the <a
   * href="http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#additional_builtin_beans">built-in
   * {@code UserTransaction} CDI bean</a>.</p>
   *
   * @return the non-{@code null} {@link UserTransaction} present in
   * this environment
   *
   * @see com.arjuna.ats.jta.UserTransaction#userTransaction()
   */
  @Override
  public final UserTransaction getUserTransaction() {
    return com.arjuna.ats.jta.UserTransaction.userTransaction();
  }

  @Override
  public final void registerTransactionSynchronization(final TransactionPhase phase,
                                                       final ObserverMethod<? super Object> observer,
                                                       final Object event)
    throws Exception {
    TransactionalEventNotifier.registerTransactionSynchronization(phase, observer, event, null /* we aren't handed metadata, so none to pass */);
  }
  
}
