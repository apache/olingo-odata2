/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;

import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraintRole;
import org.junit.BeforeClass;
import org.junit.Test;

public class EdmReferentialConstraintImplProvTest {
  private static EdmReferentialConstraintImplProv referentialConstraintProv;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {
    ReferentialConstraintRole dependent = new ReferentialConstraintRole().setRole("end1Role");
    ReferentialConstraintRole principal = new ReferentialConstraintRole().setRole("end2Role");

    ReferentialConstraint referentialConstraint = new ReferentialConstraint()
        .setDependent(dependent)
        .setPrincipal(principal);

    referentialConstraintProv = new EdmReferentialConstraintImplProv(referentialConstraint);
  }

  @Test
  public void testAssociation() throws Exception {
    EdmReferentialConstraintImplProv referentialConstraint = referentialConstraintProv;

    assertEquals("end1Role", referentialConstraint.getDependent().getRole());
    assertEquals("end2Role", referentialConstraint.getPrincipal().getRole());
  }
}
