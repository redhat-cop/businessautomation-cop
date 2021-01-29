#
# Copyright 2017 Red Hat, Inc. and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

  [when]When the credit rating is {rating:ENUM:Applicant.creditRating} = applicant:Applicant(creditRating=="{rating}")
    [then]Approve the loan = applicant.setApproved(true)
    
    [when]When the applicant dates is after {dos:DATE:default} = applicant:Applicant(applicationDate>"{dos}")
    
    [when]When the applicant approval is {bool:BOOLEAN:checked} = applicant:Applicant(approved=={bool})
    
    [when]When the ages is less than {num:1?[0-9]?[0-9]} = applicant:Applicant(age<{num})
  