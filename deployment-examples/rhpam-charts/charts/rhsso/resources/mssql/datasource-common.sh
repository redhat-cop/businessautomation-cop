#!/bin/sh

if [ -n "${TEST_LAUNCH_INCLUDE}" ]; then
    source "${TEST_LAUNCH_INCLUDE}"
else
    source $JBOSS_HOME/bin/launch/launch-common.sh
fi

if [ -n "${TEST_TX_DATASOURCE_INCLUDE}" ]; then
    source "${TEST_TX_DATASOURCE_INCLUDE}"
else
    source $JBOSS_HOME/bin/launch/tx-datasource.sh
fi

if [ -n "${TEST_LOGGING_INCLUDE}" ]; then
    source "${TEST_LOGGING_INCLUDE}"
else
    source $JBOSS_HOME/bin/launch/logging.sh
fi

function getDataSourceConfigureMode() {
  # THe extra +x makes this check whether the variable is unset, as '' is a valid value
  if [ -z ${DS_CONFIGURE_MODE+x} ]; then
    getConfigurationMode "<!-- ##DATASOURCES## -->" "DS_CONFIGURE_MODE"
  fi

  printf -v "$1" '%s' "${DS_CONFIGURE_MODE}"
}

function clearDatasourceEnv() {
  local prefix=$1
  local service=$2

  unset ${service}_SERVICE_HOST
  unset ${service}_SERVICE_PORT
  unset ${prefix}_JNDI
  unset ${prefix}_USERNAME
  unset ${prefix}_PASSWORD
  unset ${prefix}_DATABASE
  unset ${prefix}_TX_ISOLATION
  unset ${prefix}_MIN_POOL_SIZE
  unset ${prefix}_MAX_POOL_SIZE
  unset ${prefix}_JTA
  unset ${prefix}_NONXA
  unset ${prefix}_DRIVER
  unset ${prefix}_CONNECTION_CHECKER
  unset ${prefix}_EXCEPTION_SORTER
  unset ${prefix}_URL
  unset ${prefix}_BACKGROUND_VALIDATION
  unset ${prefix}_BACKGROUND_VALIDATION_MILLIS

### Start of RH-SSO add-on -- KEYCLOAK-15633:
### -----------------------------------------
### Remove / undefine also "${prefix}_CONNECTION_PROPERTY_*" env vars when
### removing / undefining "${prefix}_XA_CONNECTION_PROPERTY_*" env vars
  for property in $(compgen -v | grep -oPs "${prefix}(|_XA)_CONNECTION_PROPERTY_"); do
    unset "${property}"
  done
}
### End of RH-SSO add-on
### --------------------

function clearDatasourcesEnv() {
  IFS=',' read -a db_backends <<< $DB_SERVICE_PREFIX_MAPPING
  for db_backend in "${db_backends[@]}"; do
    service_name=${db_backend%=*}
    service=${service_name^^}
    service=${service//-/_}
    db=${service##*_}
    prefix=${db_backend#*=}

    clearDatasourceEnv $prefix $service
  done

  unset TIMER_SERVICE_DATA_STORE

  for datasource_prefix in $(echo $DATASOURCES | sed "s/,/ /g"); do
    clearDatasourceEnv $datasource_prefix $datasource_prefix
  done
  unset DATASOURCES
  unset JDBC_STORE_JNDI_NAME
  unset DS_CONFIGURE_MODE
}

# Finds the name of the database services and generates data sources
# based on this info
function inject_datasources_common() {

  inject_internal_datasources

  inject_tx_datasource

  inject_external_datasources
}

function inject_internal_datasources() {

  # keep this from polluting other scripts
  local jndi

  # Find all databases in the $DB_SERVICE_PREFIX_MAPPING separated by ","
  IFS=',' read -a db_backends <<< $DB_SERVICE_PREFIX_MAPPING

  local defaultDatasourceJndi

  if [ "${#db_backends[@]}" -eq "0" ]; then
    datasource=$(generate_datasource)
    if [ -n "$datasource" ]; then
      local dsConfMode
      getDataSourceConfigureMode "dsConfMode"
      if [ "${dsConfMode}" = "xml" ]; then
        # CIAM-1394 correction
        sed -i "s${AUS}<!-- ##DATASOURCES## -->${AUS}${datasource}<!-- ##DATASOURCES## -->${AUS}" $CONFIG_FILE
        # EOF CIAM-1394 correction
      elif [ "${dsConfMode}" = "cli" ]; then
        echo "${datasource}" >> ${CLI_SCRIPT_FILE}
      fi
    fi

    if [ -n "${ENABLE_GENERATE_DEFAULT_DATASOURCE}" ] && [ "${ENABLE_GENERATE_DEFAULT_DATASOURCE^^}" = "TRUE" ]; then
      defaultDatasourceJndi="java:jboss/datasources/ExampleDS"
    fi
  else
    for db_backend in "${db_backends[@]}"; do

      local service_name=${db_backend%=*}
      local service=${service_name^^}
      service=${service//-/_}
      local db=${service##*_}
      local prefix=${db_backend#*=}

      if [[ "$service" != *"_"* ]]; then
        log_warning "There is a problem with the DB_SERVICE_PREFIX_MAPPING environment variable!"
        log_warning "You provided the following database mapping (via DB_SERVICE_PREFIX_MAPPING): $db_backend. The mapping does not contain the database type."
        log_warning
        log_warning "Please make sure the mapping is of the form <name>-<database_type>=PREFIX, where <database_type> is either MYSQL or POSTGRESQL."
        log_warning
        log_warning "The datasource for $prefix service WILL NOT be configured."
        continue
      fi

      inject_datasource $prefix $service $service_name

      if [ -z "$defaultDatasourceJndi" ]; then
        jndi=$(get_jndi_name "$prefix" "$service")
        if [ -z "${EE_DEFAULT_DATASOURCE}" ]; then
          defaultDatasourceJndi="$jndi"
        elif [ -n "${EE_DEFAULT_DATASOURCE}" -a "${EE_DEFAULT_DATASOURCE}" = "${service_name}" ]; then
          defaultDatasourceJndi="$jndi"

          # We will use this file for validation later, so create it to indicate we found a match
          touch "${EE_DEFAULT_DATASOURCE_FILE}"
        fi
      fi
    done
  fi


  # Add things referencing our datasources to the other subsystems now that we have added all the datasources
  # For backward compatibility do the TIMER_SERVICE_DATA_STORE handling in finalVerification() so it also
  # works with 'external' datasources
  # Some more rework will be needed to do the same for the EE default datasource and the default job repository
  writeEEDefaultDatasource defaultDatasourceJndi

  if [ -z "${DEFAULT_JOB_REPOSITORY}" ]; then
    inject_hardcoded_default_job_repository
  fi
  # Add the CLI commands from file
  if [ -s "${DEFAULT_JOB_REPOSITORY_FILE}" ]; then
    # This will either be the one from the DEFAULT_JOB_REPOSITORY match, or the default one
    cat "${DEFAULT_JOB_REPOSITORY_FILE}" >> "${CLI_SCRIPT_FILE}"
  fi
}

function writeEEDefaultDatasource() {
  # Check the override and use that instead of the 'guess' if set
  if [ "${#db_backends[@]}" -gt "1" ] && [ -n "${defaultDatasourceJndi}" ] && [ -z "${EE_DEFAULT_DATASOURCE+x}" ]; then
    log_warning "The default datasource for the ee subsystem has been guessed to be ${defaultDatasourceJndi}. Specify this using EE_DEFAULT_DATASOURCE"
  fi

  # Set the default datasource
  local defaultEEDatasourceConfMode
  getConfigurationMode "##DEFAULT_DATASOURCE##" "defaultEEDatasourceConfMode"
  if [ "${defaultEEDatasourceConfMode}" = "xml" ]; then
    writeEEDefaultDatasourceXml
  else
    getConfigurationMode "<!-- ##DEFAULT_DATASOURCE## -->" "defaultEEDatasourceConfMode"
    if [ "${defaultEEDatasourceConfMode}" = "xml" ]; then
      writeEEDefaultDatasourceXml
    elif [ "${defaultEEDatasourceConfMode}" = "cli" ]; then
      writeEEDefaultDatasourceCli
    fi
  fi
}

function writeEEDefaultDatasourceXml() {
  if [ -n "$defaultDatasourceJndi" ]; then
    defaultDatasource="datasource=\"$defaultDatasourceJndi\""
  else
    defaultDatasource=""
  fi
  # new format replacement : datasource="##DEFAULT_DATASOURCE##"
  # CIAM-1394 correction
  sed -i "s${AUS}datasource=\"##DEFAULT_DATASOURCE##\"${AUS}${defaultDatasource}${AUS}" $CONFIG_FILE
  # old format (for compat)
  sed -i "s${AUS}<!-- ##DEFAULT_DATASOURCE## -->${AUS}${defaultDatasource}${AUS}" $CONFIG_FILE
  # EOF CIAM-1394 correction
}

function writeEEDefaultDatasourceCli() {
  local forcedDefaultEeDs="false"
  if [ ! -z "${EE_DEFAULT_DATASOURCE+x}" ]; then
    forcedDefaultEeDs="true"
  fi

  local xpath="\"//*[local-name()='subsystem' and starts-with(namespace-uri(), 'urn:jboss:domain:ee:')]\""
  local ret
  testXpathExpression "${xpath}" "ret"
  if [ "${ret}" -ne 0 ]; then
    if [ "${forcedDefaultEeDs}" = "true" ]; then
      log_error "EE_DEFAULT_DATASOURCE was set to '${EE_DEFAULT_DATASOURCE}' but the base configuration contains no ee subsystem. Fix your configuration. "
      exit 1
    else
      # We have no ee subsystem and have just guessed what should go in - this is fine
      return
    fi
  fi

  local resource="/subsystem=ee/service=default-bindings"
  # Add the default bindings if not there
  echo "
    if (outcome != success) of $resource:read-resource
      $resource:add
    end-if
  " >> ${CLI_SCRIPT_FILE}


  xpath="\"//*[local-name()='default-bindings' and starts-with(namespace-uri(), 'urn:jboss:domain:ee:')]/@datasource\""
  ret=""
  testXpathExpression "${xpath}" "ret"

  local writeDs="$resource:write-attribute(name=datasource, value=${defaultDatasourceJndi})"
  local cli_action
  if [ "${ret}" -eq 0 ]; then
    # Attribute exists in config already
    if [ -n "${defaultDatasourceJndi}" ]; then
      # Base config already has a value, what happens next depends on if it was forced or guessed
      if [ "${forcedDefaultEeDs}" = true ]; then
        # We forced it, so log an error and exit if we have a conflict between the base config and the env var setting
        cli_action="
          if (result != \"${defaultDatasourceJndi}\") of ${resource}:read-attribute(name=datasource)
            echo You have set environment variables to configure the datasource in the default-bindings in the ee subsystem subsystem which conflicts with the value that already exists in the base configuration. Fix your configuration. >> \${error_file}
            exit
          end-if"
      else
        # We guessed it, so log a warning if we have a conflict between the base config and the env var setting
        cli_action="
          if (result != \"${defaultDatasourceJndi}\") of ${resource}:read-attribute(name=datasource)
            echo You have set environment variables to configure the datasource in the default-bindings in the ee subsystem subsystem which conflicts with the value that already exists in the base configuration. The base configuration value will be used. Fix your configuration. >> \${warning_file}
          end-if"
      fi
    fi
  else
    # Attribute does not exist in config already, so write whatever was defined
    if [ -n "${defaultDatasourceJndi}" ]; then
      cli_action="${writeDs}"
    fi
  fi

  if [ -n "${cli_action}" ]; then
    echo "
        ${cli_action}
      " >> ${CLI_SCRIPT_FILE}
  fi
}

function inject_external_datasources() {
  # Add extensions from envs
  if [ -n "$DATASOURCES" ]; then
    for datasource_prefix in $(echo $DATASOURCES | sed "s/,/ /g"); do
      inject_datasource $datasource_prefix $datasource_prefix $datasource_prefix
    done
  fi
}

# Arguments:
# $1 - service name
# $2 - datasource jndi name
# $3 - datasource username
# $4 - datasource password
# $5 - datasource host
# $6 - datasource port
# $7 - datasource databasename
# $8 - connection checker class
# $9 - exception sorter class
# $10 - driver
# $11 - original service name
# $12 - datasource jta
# $13 - validate
# $14 - url
function generate_datasource_common() {
  log_info "[MSSQL] Called generate_datasource_common"
  local pool_name="${1}"
  local jndi_name="${2}"
  local username="${3}"
  local password="${4}"
  local host="${5}"
  local port="${6}"
  local databasename="${7}"
  local checker="${8}"
  local sorter="${9}"
  local driver="${10}"
  local service_name="${11}"
  local jta="${12}"
  local validate="${13}"
  local url="${14}"

  local dsConfMode
  getDataSourceConfigureMode "dsConfMode"
  if [ "${dsConfMode}" = "xml" ]; then
    # CLOUD-3198 Since Sed replaces '&' with a full match, we need to escape it.
    local url="${14//&/\\&}"
    # CLOUD-3198 In addition to that, we also need to escape ';'
    url="${url//;/\\;}"
  fi

  if [ -n "$driver" ]; then
    ds=$(generate_external_datasource)
  else
    jndi_name="java:jboss/datasources/ExampleDS"
    if [ -n "$DB_JNDI" ]; then
      jndi_name="$DB_JNDI"
    fi
    pool_name="ExampleDS"
    if [ -n "$DB_POOL" ]; then
      pool_name="$DB_POOL"
    fi

    # Scripts that want to enable addition of the default data source should set
    # ENABLE_GENERATE_DEFAULT_DATASOURCE=true
    if [ -n "${ENABLE_GENERATE_DEFAULT_DATASOURCE}" ] && [ "${ENABLE_GENERATE_DEFAULT_DATASOURCE^^}" = "TRUE" ]; then
      ds=$(generate_default_datasource)
    fi
  fi

  if [ -z "$service_name" ]; then
    if [ -n "${ENABLE_GENERATE_DEFAULT_DATASOURCE}" ] && [ "${ENABLE_GENERATE_DEFAULT_DATASOURCE^^}" = "TRUE" ]; then
      service_name="ExampleDS"
      driver="hsql"
      pool_name="ExampleDS"
      if [ -n "$DB_POOL" ]; then
        pool_name="$DB_POOL"
      fi
    else
      return
    fi
  fi

  if [ -n "$TIMER_SERVICE_DATA_STORE" -a "$TIMER_SERVICE_DATA_STORE" = "${service_name}" ]; then
    inject_timer_service ${pool_name} ${jndi_name} ${driver} ${TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL:--1}
  fi
  if [ -n "$DEFAULT_JOB_REPOSITORY" -a "$DEFAULT_JOB_REPOSITORY" = "${service_name}" ]; then
    inject_job_repository "${pool_name}"
    inject_default_job_repository "${pool_name}"
  fi

  if [ "${dsConfMode}" = "xml" ]; then
    # Only do this replacement if we are replacing an xml marker
    echo "$ds" | sed ':a;N;$!ba;s|\n|\\n|g'
  elif [ "${dsConfMode}" = "cli" ]; then
    # If using cli, return the raw string, preserving line breaks
    echo "$ds"
  fi
}

function generate_external_datasource() {
  local dsConfMode
  getDataSourceConfigureMode "dsConfMode"
  if [ "${dsConfMode}" = "xml" ]; then
    echo "$(generate_external_datasource_xml)"
  elif [ "${dsConfMode}" = "cli" ]; then
    echo "$(generate_external_datasource_cli)"
  fi
}

### Start of RH-SSO add-on -- KEYCLOAK-15633:
### -----------------------------------------
### Allow specification of datasource connection properties for:
### 1) XA datasources via the DB_XA_CONNECTION_PROPERTY_<property_name> and
### 2) Non-XA datasources via the DB_CONNECTION_PROPERTY_<property_name>
###
### environment variable(s) defined on the container image. The <property_name>
### in the above expressions is the actual name of the connection property to
### be set on the underlying datasource.
###
function inject_connection_properties_to_datasource_xml() {
  local ds="${1}"

  local failed="false"
  local is_xa_ds="false"

  if [[ "${ds}" =~ \<datasource[[:space:]].*$ ]]; then
    local conn_prop_env_var="${prefix}_CONNECTION_PROPERTY_"
    local conn_prop_xml_elem_name="connection-property"
  elif [[ "${ds}" =~ \<xa-datasource[[:space:]].*$ ]]; then
    local conn_prop_env_var="${prefix}_XA_CONNECTION_PROPERTY_"
    local conn_prop_xml_elem_name="xa-datasource-property"
    is_xa_ds="true"
  else
    log_warning "Unable to determine if '${ds}' datasource is a non-XA or XA one."
    log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
    failed="true"
  fi

  declare -ra conn_props=($(compgen -v | grep -s "${conn_prop_env_var}"))
  if [ "${is_xa_ds}" == "true" ] && [ -z "${conn_props}" ]; then
    log_warning "At least one ${prefix}_XA_CONNECTION_PROPERTY_property for datasource ${service_name} is required."
    log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
    failed="true"
  fi

  if [ "${failed}" != "true" ]; then
    for property in "${conn_props[@]}"; do
      # CIAM-1394 correction
      prop_name=$(sed -e "s${AUS}${conn_prop_env_var}${AUS}${AUS}g" <<< "${property}")
      # EOF CIAM-1394 correction
      prop_value=$(find_env "${property}")
      if [ ! -z "${prop_value}" ]; then
          ds="${ds}
               <${conn_prop_xml_elem_name} name=\"${prop_name}\">${prop_value}</${conn_prop_xml_elem_name}>"
      fi
    done
  fi

  # 'ds' value of empty string indicates an error occurred earlier
  if [ "${failed}" == "true" ]; then
    echo ""
  else
    echo "${ds}"
  fi
}
### End of RH-SSO add-on
### --------------------

function generate_external_datasource_xml() {

### Start of RH-SSO add-on -- KEYCLOAK-15633:
### -----------------------------------------
### Modify the 'generate_external_datasource_xml()' method to
### allow specification of connection properties also for non-XA datasources

  local failed="false"
  local ds_universal_attrs="jndi-name=\"${jndi_name}\" pool-name=\"${pool_name}\" enabled=\"true\" use-java-context=\"true\" statistics-enabled=\"\${wildfly.datasources.statistics-enabled:\${wildfly.statistics-enabled:false}}\""

  if [ -n "$NON_XA_DATASOURCE" ] && [ "$NON_XA_DATASOURCE" = "true" ]; then
    ds="<datasource jta=\"${jta}\" ${ds_universal_attrs}>
          <connection-url>${url}</connection-url>"
  else
    ds="<xa-datasource ${ds_universal_attrs}>"
  fi

  ds=$(inject_connection_properties_to_datasource_xml "${ds}")
  # 'ds' value of empty string indicates an error occurred earlier
  if [ "${ds}" == "" ]; then
    failed="true"
  fi

  ds="$ds
       <driver>${driver}</driver>"
### End of RH-SSO add-on
### --------------------

  if [ -n "$tx_isolation" ]; then
    ds="$ds
            <transaction-isolation>$tx_isolation</transaction-isolation>"
  fi

  if [ -n "$min_pool_size" ] || [ -n "$max_pool_size" ]; then
    if [ -n "$NON_XA_DATASOURCE" ] && [ "$NON_XA_DATASOURCE" = "true" ]; then
      ds="$ds
             <pool>"
    else
      ds="$ds
             <xa-pool>"
    fi

    if [ -n "$min_pool_size" ]; then
      ds="$ds
             <min-pool-size>$min_pool_size</min-pool-size>"
    fi
    if [ -n "$max_pool_size" ]; then
      ds="$ds
             <max-pool-size>$max_pool_size</max-pool-size>"
    fi
    if [ -n "$NON_XA_DATASOURCE" ] && [ "$NON_XA_DATASOURCE" = "true" ]; then
      ds="$ds
             </pool>"
    else
      ds="$ds
             </xa-pool>"
    fi
  fi

   ds="$ds
         <security>
           <user-name>${username}</user-name>
           <password>${password}</password>
         </security>"

  if [ "$validate" == "true" ]; then

    validation_conf="<validate-on-match>true</validate-on-match>
                       <background-validation>false</background-validation>"

    if [ $(find_env "${prefix}_BACKGROUND_VALIDATION" "false") == "true" ]; then

        millis=$(find_env "${prefix}_BACKGROUND_VALIDATION_MILLIS" 10000)
        validation_conf="<validate-on-match>false</validate-on-match>
                           <background-validation>true</background-validation>
                           <background-validation-millis>${millis}</background-validation-millis>"
    fi

    ds="$ds
           <validation>
             ${validation_conf}
             <valid-connection-checker class-name=\"${checker}\"></valid-connection-checker>
             <exception-sorter class-name=\"${sorter}\"></exception-sorter>
           </validation>"
  fi

  if [ -n "$NON_XA_DATASOURCE" ] && [ "$NON_XA_DATASOURCE" = "true" ]; then
    ds="$ds
           </datasource>"
  else
    ds="$ds
           </xa-datasource>"
  fi
  log_info "[MSSQL] ds is ${ds}"

  if [ "$failed" == "true" ]; then
    echo ""
  else
    echo $ds
  fi
}

### Start of RH-SSO add-on -- KEYCLOAK-15633:
### -----------------------------------------
### Allow specification of datasource connection properties for:
### 1) XA datasources via the DB_XA_CONNECTION_PROPERTY_<property_name> and
### 2) Non-XA datasources via the DB_CONNECTION_PROPERTY_<property_name>
###
### environment variable(s) defined on the container image. The <property_name>
### in the above expressions is the actual name of the connection property to
### be set on the underlying datasource.
###
function generate_external_datasource_cli() {
  local failed="false"
  local is_xa_ds="false"
  local subsystem_addr="/subsystem=datasources"
  local ds_resource="${subsystem_addr}"
  local other_ds_resource

  local -A ds_tmp_key_values
  ds_tmp_key_values["jndi-name"]=${jndi_name}
  ds_tmp_key_values["enabled"]="true"
  ds_tmp_key_values["use-java-context"]="true"
  ds_tmp_key_values["statistics-enabled"]="\${wildfly.datasources.statistics-enabled:\${wildfly.statistics-enabled:false}}"
  ds_tmp_key_values["driver-name"]="${driver}"

  local -A ds_tmp_connection_properties

  if [ -n "$NON_XA_DATASOURCE" ] && [ "$NON_XA_DATASOURCE" = "true" ]; then
    ds_resource="${subsystem_addr}/data-source=${pool_name}"
    other_ds_resource="${subsystem_addr}/xa-data-source=${pool_name}"

    local conn_prop_env_var="${prefix}_CONNECTION_PROPERTY_"
    local conn_prop_cli_elem_name="connection-properties"

    ds_tmp_key_values["jta"]="${jta}"
    ds_tmp_key_values['connection-url']="${url}"

  else
    ds_resource="${subsystem_addr}/xa-data-source=${pool_name}"
    other_ds_resource="${subsystem_addr}/data-source=${pool_name}"

    local conn_prop_env_var="${prefix}_XA_CONNECTION_PROPERTY_"
    local conn_prop_cli_elem_name="xa-datasource-properties"
    is_xa_ds="true"

    declare -ra conn_props=($(compgen -v | grep -s "${conn_prop_env_var}"))
    if [ "${is_xa_ds}" == "true" ] && [ -z "${conn_props}" ]; then
      log_warning "At least one ${prefix}_XA_CONNECTION_PROPERTY_property for datasource ${service_name} is required."
      log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
      failed="true"
    else
      if [ "${failed}" != "true" ]; then
        for property in "${conn_props[@]}"; do
          # CIAM-1394 correction
          prop_name=$(sed -e "s${AUS}${conn_prop_env_var}${AUS}${AUS}g" <<< "${property}")
          # EOF CIAM-1394 correction
          prop_value=$(find_env "${property}")
          if [ ! -z "${prop_value}" ]; then
            ds_tmp_connection_properties["${prop_name}"]="${prop_value}"
          fi
        done
      fi
    fi
  fi

  if [ -n "${tx_isolation}" ]; then
    ds_tmp_key_values["transaction-isolation"]="${tx_isolation}"
  fi

  if [ -n "$min_pool_size" ]; then
    ds_tmp_key_values["min-pool-size"]=$min_pool_size
  fi
  if [ -n "$max_pool_size" ]; then
    ds_tmp_key_values["max-pool-size"]=$max_pool_size
  fi

  ds_tmp_key_values["user-name"]="${username}"
  ds_tmp_key_values["password"]="${password}"

  if [ "$validate" == "true" ]; then

    ds_tmp_key_values["validate-on-match"]="true"
    ds_tmp_key_values["background-validation"]="false"

    if [ $(find_env "${prefix}_BACKGROUND_VALIDATION" "false") == "true" ]; then

        millis=$(find_env "${prefix}_BACKGROUND_VALIDATION_MILLIS" 10000)
        ds_tmp_key_values["validate-on-match"]="false"
        ds_tmp_key_values["background-validation"]="true"
        ds_tmp_key_values["background-validation-millis"]="${millis}"
    fi

    ds_tmp_key_values["valid-connection-checker-class-name"]="${checker}"
    ds_tmp_key_values["exception-sorter-class-name"]="${sorter}"
  fi

  ###########################################
  # Construct the CLI part

  # Create the add operation
  local ds_tmp_add="$ds_resource:add("
  local tmp_separator=""
  for key in "${!ds_tmp_key_values[@]}"; do
    ds_tmp_add="${ds_tmp_add}${tmp_separator}${key}=\"${ds_tmp_key_values[$key]}\""
    tmp_separator=", "
  done
  ds_tmp_add="${ds_tmp_add})"

  # Add the connection properties to both XA & non-XA datasource
  local ds_tmp_conn_props_add
  for key in "${!ds_tmp_connection_properties[@]}"; do
    ds_tmp_conn_props_add="${ds_tmp_conn_props_add}
        $ds_resource/${conn_prop_cli_elem_name}=${key}:add(value=\"${ds_tmp_connection_properties[$key]}\")
    "
  done

  # We check if the datasource is there and remove it before re-adding in a batch.
  # Otherwise we simply add it. Unfortunately CLI control flow does not work when wrapped
  # in a batch

  ds="
    if (outcome != success) of ${subsystem_addr}:read-resource
      echo You have set environment variables to configure the datasource '${pool_name}'. Fix your configuration to contain a datasources subsystem for this to happen. >> \${error_file}
      exit
    end-if

    if (outcome == success) of ${ds_resource}:read-resource
      echo You have set environment variables to configure the datasource '${pool_name}'. However, your base configuration already contains a datasource with that name. >> \${error_file}
      exit
    end-if

    if (outcome == success) of ${other_ds_resource}:read-resource
      echo You have set environment variables to configure the datasource '${pool_name}'. However, your base configuration already contains a datasource with that name. >> \${error_file}
      exit
    end-if

    batch
    ${ds_tmp_add}
    ${ds_tmp_conn_props_add}
    run-batch
  "
  log_info "[MSSQL] generate_external_datasource_cli: ds is ${ds}"
### End of RH-SSO add-on
### --------------------

  if [ "$failed" == "true" ]; then
    echo ""
  else
    echo "$ds"
  fi
}

function generate_default_datasource() {

  local ds_tmp_url=""

  if [ -n "$url" ]; then
    ds_tmp_url="${url}"
  else
    ds_tmp_url="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
  fi

  local dsConfMode
  getDataSourceConfigureMode "dsConfMode"
  if [ "${dsConfMode}" = "xml" ]; then
    echo "$(generate_default_datasource_xml $ds_tmp_url)"
  elif [ "${dsConfMode}" = "cli" ]; then
    echo "$(generate_default_datasource_cli $ds_tmp_url)"
  fi
}

function generate_default_datasource_xml() {
  local ds_tmp_url=$1

  ds="<datasource jta=\"true\" jndi-name=\"${jndi_name}\" pool-name=\"${pool_name}\" enabled=\"true\" use-java-context=\"true\" statistics-enabled=\"\${wildfly.datasources.statistics-enabled:\${wildfly.statistics-enabled:false}}\">
    <connection-url>${ds_tmp_url}</connection-url>"

  ds="$ds
        <driver>h2</driver>
          <security>
            <user-name>sa</user-name>
            <password>sa</password>
          </security>
        </datasource>"

  echo $ds
}

function generate_default_datasource_cli() {
  local ds_tmp_url=$1

  local subsystem_addr="/subsystem=datasources"
  local ds_resource="${subsystem_addr}/data-source=${pool_name}"
  local xa_resource="${subsystem_addr}/xa-data-source=${pool_name}"
  # Here we assume that if the default DS was created any other way, we we give an error.

  ds="
    if (outcome != success) of ${subsystem_addr}:read-resource
      echo You have set environment variables to configure the default datasource '${pool_name}'. Fix your configuration to contain a datasources subsystem for this to happen. >> \${error_file}
      exit
    end-if

    if (outcome == success) of ${ds_resource}:read-resource
      echo You have set environment variables to configure the default datasource '${pool_name}'. However, your base configuration already contains a datasource with that name. >> \${error_file}
      exit
    end-if

    if (outcome == success) of ${xa_resource}:read-resource
      echo You have set environment variables to configure the default datasource '${pool_name}'. However, your base configuration already contains a datasource with that name. >> \${error_file}
      exit
    end-if

    $ds_resource:add(jta=true, jndi-name=${jndi_name}, enabled=true, use-java-context=true, statistics-enabled=\${wildfly.datasources.statistics-enabled:\${wildfly.statistics-enabled:false}}, driver-name=h2, user-name=sa, password=sa, connection-url=\"${ds_tmp_url}\")
"
  echo "$ds"
}

function inject_default_timer_service() {
  local confMode
  getConfigurationMode "<!-- ##TIMER_SERVICE## -->" "confMode"
  if [ "$confMode" = "xml" ]; then
    local timerservice="            <timer-service thread-pool-name=\"default\" default-data-store=\"default-file-store\">\
                  <data-stores>\
                      <file-data-store name=\"default-file-store\" path=\"timer-service-data\" relative-to=\"jboss.server.data.dir\"/>\
                  </data-stores>\
              </timer-service>"
    # CIAM-1394 correction
    sed -i "s${AUS}<!-- ##TIMER_SERVICE## -->${AUS}${timerservice}${AUS}" $CONFIG_FILE
    # EOF CIAM-1394 correction

    # We will use this file for validation later, so write here that we found a match
    touch "${TIMER_SERVICE_DATA_STORE_FILE}"
  elif [ "$confMode" = "cli" ]; then
    local hasEjb3Subsystem
    local xpath="\"//*[local-name()='subsystem' and starts-with(namespace-uri(), 'urn:jboss:domain:ejb3:')]\""
    testXpathExpression "${xpath}" "hasEjb3Subsystem"
    if [ $hasEjb3Subsystem -eq 0 ]; then
      # Since we are adding a default, we only do this if we have an ejb3 subsystem
      local timerResource="/subsystem=ejb3/service=timer-service"
      # Only add this if there is no timer service already existing in the config
      local cli="
        if (outcome != success) of ${timerResource}:read-resource
          batch
          ${timerResource}:add(thread-pool-name=default, default-data-store=default-file-store)
          ${timerResource}/file-data-store=default-file-store:add(path=timer-service-data, relative-to=jboss.server.data.dir)
          run-batch
        end-if
      "
      # Since this is happening as part of the datasource generation, in a subshell, and these
      # commands need to happen AFTER the datasource has been added, write the CLI commands out
      # to a temp file that we will read later
      echo "${cli}" >> "${TIMER_SERVICE_DATA_STORE_FILE}"
    fi
  fi
}

# $1 - service/pool name
# $2 - datasource jndi name
# $3 - datasource databasename
# $4 - datastore refresh-interval (only applicable on eap7.x)
function inject_timer_service() {
  local pool_name="${1}"
  local datastore_name="${pool_name}"_ds
  local jndi_name="${2}"
  local databasename="${3}"
  local refresh_interval="${4}"

  local confMode
  getConfigurationMode "<!-- ##TIMER_SERVICE## -->" "confMode"
  if [ "$confMode" = "xml" ]; then
    local timerservice="            <timer-service thread-pool-name=\"default\" default-data-store=\"${datastore_name}\">\
                  <data-stores>\
                    <database-data-store name=\"${datastore_name}\" datasource-jndi-name=\"${jndi_name}\" database=\"${databasename}\" partition=\"${pool_name}_part\" refresh-interval=\"${refresh_interval}\"/>
                  </data-stores>\
              </timer-service>"
    # CIAM-1394 correction
    sed -i "s${AUS}<!-- ##TIMER_SERVICE## -->${AUS}${timerservice}${AUS}" $CONFIG_FILE
    # EOF CIAM-1394 correction

    # We will use this file for validation later, so write here that we found a match
    touch "${TIMER_SERVICE_DATA_STORE_FILE}"
  elif [ "$confMode" = "cli" ]; then
    local hasEjb3Subsystem
    local xpath="\"//*[local-name()='subsystem' and starts-with(namespace-uri(), 'urn:jboss:domain:ejb3:')]\""
    testXpathExpression "${xpath}" "hasEjb3Subsystem"
    if [ $hasEjb3Subsystem -ne 0 ]; then
      # No ejb3 subsystem is an error. We need to push this into the error file since this runs inside a sub-shell and
      # any echo without a redirect here goes to the CLI file. Also any attempt to exit here only exits the sub-shell,
      # Not the whole launch process
      echo "You have set the TIMER_SERVICE_DATA_STORE environment variable which adds a timer-service to the ejb3 subsystem. Fix your configuration to contain an ejb3 subsystem for this to happen." >> ${CONFIG_ERROR_FILE}
      exit 1
    fi
    local timerResource="/subsystem=ejb3/service=timer-service"
    local datastoreResource="${timerResource}/database-data-store=${datastore_name}"
    local datastoreAdd="
      ${datastoreResource}:add(datasource-jndi-name=${jndi_name}, database=${databasename}, partition=${pool_name}_part, refresh-interval=${refresh_interval})"
    # We add the timer-service and the datastore in a batch if it is not there
    local cli="
      if (outcome != success) of ${timerResource}:read-resource
        batch
        ${timerResource}:add(thread-pool-name=default, default-data-store=${datastore_name})
        ${datastoreAdd}
        run-batch
      end-if"
    # Next we add the datastore if not there. This will work both if we added it in the previous line, or if the
    # user supplied a configuration that already contained the timer service but not the desired datastore
    cli="${cli}
      if (outcome != success) of ${datastoreResource}:read-resource
        ${datastoreAdd}
      end-if"
    # Next we do a check to see if the datastore contains the same values as calculated from the variables.
    # This is needed for the case when the base configuration already contained it, so the adds above
    # would not have taken effect.
    cli="${cli}
      if (result.allow-execution != true || result.database != \"${databasename}\" || result.datasource-jndi-name != \"${jndi_name}\" || result.partition != \"${pool_name}_part\" || result.refresh-interval != ${refresh_interval})  of /subsystem=ejb3/service=timer-service/database-data-store=test_mysql-TEST_ds:query(select=[\"allow-execution\", \"database\", \"datasource-jndi-name\", \"partition\", \"refresh-interval\"])
        echo You have set environment variables to configure a timer service database-data-store in the ejb3 subsystem which conflict with the values that already exist in the base configuration. Fix your configuration. >> \${error_file}
        exit
      end-if
    "
    #Finally we write the default-data-store attribute, which should work whether we added the
    #timer-service or the datastore or not
    cli="${cli}
      ${timerResource}:write-attribute(name=default-data-store, value=${datastore_name})
    "
    # Since this is happening as part of the datasource generation, in a subshell, and these
    # commands need to happen AFTER the datasource has been added, write the CLI commands out
    # to a temp file that we will read later
    echo "${cli}" >> "${TIMER_SERVICE_DATA_STORE_FILE}"
  fi
}

function map_properties() {
  local protocol=${1}
  local serverNameVar=${2}
  local portVar=${3}
  local databaseNameVar=${4}
  local invalidVar=${5}

  local hasAllUrlParts="false"
  if [ -n "$host" ] && [ -n "$port" ] && [ -n "$database" ]; then
    hasAllUrlParts="true"
  fi

  if [ -n "${url}" ] || [ "${hasAllUrlParts}" = true ]; then
    if [ -z "$url" ]; then
      url="${protocol}://${host}:${port}/${database}"
    fi

    if [ "$NON_XA_DATASOURCE" == "false" ] && [ -z "$(eval echo \$${prefix}_XA_CONNECTION_PROPERTY_URL)" ]; then
      # It is an XA datasource
      if [ -z "${!serverNameVar}" ]; then
        eval ${serverNameVar}=${host}
      fi

      if [ -z "${!portVar}" ]; then
        eval ${portVar}=${port}
      fi

      if [ -z "${!databaseNameVar}" ]; then
        eval ${databaseNameVar}=${database}
      fi
    fi
  elif [ "$NON_XA_DATASOURCE" == "false" ]; then
    # It is an XA datasource
    if [ -z "$(eval echo \$${prefix}_XA_CONNECTION_PROPERTY_URL)" ]; then
      if [ -z "${!serverNameVar}" ] || [ -z "${!portVar}" ] || [ -z "${!databaseNameVar}" ]; then
        if [ "$prefix" != "$service" ]; then
          log_warning "Missing configuration for datasource $prefix. ${service}_SERVICE_HOST, ${service}_SERVICE_PORT, and/or ${prefix}_DATABASE is missing."
          log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
          eval ${invalidVar}="true"
        else
          log_warning "Missing configuration for XA datasource $prefix. Either ${prefix}_XA_CONNECTION_PROPERTY_URL or $serverNameVar, and $portVar, and $databaseNameVar is required."
          log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
          eval ${invalidVar}="true"
        fi
      else
        host="${!serverNameVar}"
        port="${!portVar}"
        database="${!databaseNameVar}"
      fi
    fi
  else
    log_warning "Missing configuration for datasource $prefix. ${service}_SERVICE_HOST, ${service}_SERVICE_PORT, and/or ${prefix}_DATABASE is missing."
    log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
    eval ${invalidVar}="true"
  fi

}

function inject_datasource() {
  local prefix=$1
  local service=$2
  local service_name=$3

  local host
  local port
  local jndi
  local username
  local password
  local database
  local tx_isolation
  local min_pool_size
  local max_pool_size
  local jta
  local NON_XA_DATASOURCE
  local driver
  local validate
  local checker
  local sorter
  local url
  local service_name

  host=$(find_env "${service}_SERVICE_HOST")

  port=$(find_env "${service}_SERVICE_PORT")

  # Custom JNDI environment variable name format: [NAME]_[DATABASE_TYPE]_JNDI
  jndi=$(get_jndi_name "$prefix" "$service")

  # Database username environment variable name format: [NAME]_[DATABASE_TYPE]_USERNAME
  username=$(find_env "${prefix}_USERNAME")

  # Database password environment variable name format: [NAME]_[DATABASE_TYPE]_PASSWORD
  password=$(find_env "${prefix}_PASSWORD")

  # Database name environment variable name format: [NAME]_[DATABASE_TYPE]_DATABASE
  database=$(find_env "${prefix}_DATABASE")

  if [ -z "$jndi" ] || [ -z "$username" ] || [ -z "$password" ]; then
    log_warning "Ooops, there is a problem with the ${db,,} datasource!"
    log_warning "In order to configure ${db,,} datasource for $prefix service you need to provide following environment variables: ${prefix}_USERNAME and ${prefix}_PASSWORD."
    log_warning
    log_warning "Current values:"
    log_warning
    log_warning "${prefix}_USERNAME: $username"
    log_warning "${prefix}_PASSWORD: $password"
    log_warning "${prefix}_JNDI: $jndi"
    log_warning
    log_warning "The ${db,,} datasource for $prefix service WILL NOT be configured."
    return
  fi

  # Transaction isolation level environment variable name format: [NAME]_[DATABASE_TYPE]_TX_ISOLATION
  tx_isolation=$(find_env "${prefix}_TX_ISOLATION")

  # min pool size environment variable name format: [NAME]_[DATABASE_TYPE]_MIN_POOL_SIZE
  min_pool_size=$(find_env "${prefix}_MIN_POOL_SIZE")

  # max pool size environment variable name format: [NAME]_[DATABASE_TYPE]_MAX_POOL_SIZE
  max_pool_size=$(find_env "${prefix}_MAX_POOL_SIZE")

  # jta environment variable name format: [NAME]_[DATABASE_TYPE]_JTA
  jta=$(find_env "${prefix}_JTA" true)

  # $NON_XA_DATASOURCE: [NAME]_[DATABASE_TYPE]_NONXA (DB_NONXA)
  NON_XA_DATASOURCE=$(find_env "${prefix}_NONXA" false)

  url=$(find_env "${prefix}_URL")
  driver=$(find_env "${prefix}_DRIVER" )
  if [ -z "${driver}" ] && [ -n "${db}" ]; then
    # $db is set by inject_internal_datasources and attempts to
    # extract a driver name from the DB_SERVICE_PREFIX_MAPPING entry and puts it in upper case.
    # If it is MYSQL or POSTGRESQL we use that as the driver name
    if [ "${db}" = "MYSQL" ] || [ "${db}" = "POSTGRESQL" ] || [ "${db}" = "MSSQL" ]; then
      driver="${db,,}"
    fi
  fi

  checker=$(find_env "${prefix}_CONNECTION_CHECKER" )
  sorter=$(find_env "${prefix}_EXCEPTION_SORTER" )

  if [ -n "$checker" ] && [ -n "$sorter" ]; then
    validate="true"
  else
    validate="false"
  fi

  local invalid
  case "${driver}" in
      "mysql")
        map_properties "jdbc:mysql" "${prefix}_XA_CONNECTION_PROPERTY_ServerName" "${prefix}_XA_CONNECTION_PROPERTY_Port" "${prefix}_XA_CONNECTION_PROPERTY_DatabaseName" "invalid"
        if [ "${validate}" = "false" ]; then
          validate="true"
          checker="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker"
          sorter="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter"
        fi
      ;;
      "postgresql")
        map_properties "jdbc:postgresql" "${prefix}_XA_CONNECTION_PROPERTY_ServerName" "${prefix}_XA_CONNECTION_PROPERTY_PortNumber" "${prefix}_XA_CONNECTION_PROPERTY_DatabaseName" "invalid"
        if [ "${validate}" = "false" ]; then
          validate="true"
          checker="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"
          sorter="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"
        fi
      ;;
      "mssql")
        map_properties "jdbc:sqlserver" "${prefix}_XA_CONNECTION_PROPERTY_ServerName" "${prefix}_XA_CONNECTION_PROPERTY_PortNumber" "${prefix}_XA_CONNECTION_PROPERTY_DatabaseName" "invalid"
        if [ "${validate}" = "false" ]; then
          validate="true"
          checker="org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker"
          sorter="org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLExceptionSorter"
        fi
      ;;
      *)
        service_name=$prefix
        ;;
  esac

  if [ "${invalid}" = "true" ]; then
    return
  fi

  if [ -z "$jta" ]; then
    log_warning "JTA flag not set, defaulting to true for datasource  ${service_name}"
    jta=true
  fi

  if [ -z "$driver" ]; then
    log_warning "DRIVER not set for datasource ${service_name}."
    log_warning "Datasource '$(basename ${jndi_name})' will not be configured."
  else
    datasource=$(generate_datasource "${service,,}-${prefix}" "$jndi" "$username" "$password" "$host" "$port" "$database" "$checker" "$sorter" "$driver" "$service_name" "$jta" "$validate" "$url")

    if [ -n "$datasource" ]; then
      local dsConfMode
      getDataSourceConfigureMode "dsConfMode"
      if [ "${dsConfMode}" = "xml" ]; then
        # CIAM-1394 correction
        sed -i "s${AUS}<!-- ##DATASOURCES## -->${AUS}${datasource}\n<!-- ##DATASOURCES## -->${AUS}" $CONFIG_FILE
        # EOF CIAM-1394 correction
      elif [ "${dsConfMode}" = "cli" ]; then
        echo "${datasource}" >> ${CLI_SCRIPT_FILE}
      fi
    fi

  fi
}

function get_jndi_name() {
  local prefix=$1
  echo $(find_env "${prefix}_JNDI" "java:jboss/datasources/${service,,}")
}

function inject_hardcoded_default_job_repository() {
  inject_default_job_repository "in-memory" "hardcoded"
}

# Arguments:
# $1 - default job repository name
function inject_default_job_repository() {
  local hardcoded="${2}"
  local dsConfMode
  getConfigurationMode "<!-- ##DEFAULT_JOB_REPOSITORY## -->" "dsConfMode"
  if [ "${dsConfMode}" = "xml" ]; then
    local defaultjobrepo="     <default-job-repository name=\"${1}\"/>"
    # CIAM-1394 correction
    sed -i "s${AUS}<!-- ##DEFAULT_JOB_REPOSITORY## -->${AUS}${defaultjobrepo%$'\n'}${AUS}" $CONFIG_FILE
    # EOF CIAM-1394 correction

    # We will use this file for validation later, so create it to indicate we found a match
    touch "${DEFAULT_JOB_REPOSITORY_FILE}"
  elif [ "${dsConfMode}" = "cli" ]; then

    local resourceAddr="/subsystem=batch-jberet"
    if [ -z "${hardcoded}" ] ; then
      # We only need to do something when the user has explicitly set a default job repository.
      # This is because the base configuration needs to have a job repository set up for CLI
      # replacement to work as the CLI embedded server will not even boot if it is not there.
      # (in the xml marker replacement it works differently as we replace the marker with the xml
      # for the default repo).
      # The hardcoded default-job-repository should only be set if there is a batch-jberet
      # subsystem. If the user specified the DEFAULT_JOB_REPOSITORY variable, and there is no
      # batch-jberet subsystem, this will give an error in inject_job_repository() so there is
      # no need to do that again here.
      local cli="
      if (outcome == success) of ${resourceAddr}:read-resource
        ${resourceAddr}:write-attribute(name=default-job-repository, value=${1})
      end-if
      "

      # Since this is happening as part of the datasource generation, in a subshell, and these
      # commands need to happen AFTER the datasource has been added, write the CLI commands out
      # to a temp file that we will read later
      echo "${cli}" >> "${DEFAULT_JOB_REPOSITORY_FILE}"
    fi
  fi
}

# Arguments:
# $1 - job repository name
function inject_job_repository() {
  local dsConfMode
  getConfigurationMode "<!-- ##JOB_REPOSITORY## -->" "dsConfMode"
  if [ "${dsConfMode}" = "xml" ]; then
    local jobrepo="     <job-repository name=\"${1}\">\
        <jdbc data-source=\"${1}\"/>\
      </job-repository>\
      <!-- ##JOB_REPOSITORY## -->"

    # CIAM-1394 correction
    sed -i "s${AUS}<!-- ##JOB_REPOSITORY## -->${AUS}${jobrepo%$'\n'}${AUS}" $CONFIG_FILE
    # EOF CIAM-1394 correction

    # We will use this file for validation later, so create it to indicate we found a match
    touch "${DEFAULT_JOB_REPOSITORY_FILE}"
  elif [ "${dsConfMode}" = "cli" ]; then
    local subsystemAddr="/subsystem=batch-jberet"
    local resourceAddr="${subsystemAddr}/jdbc-job-repository=${1}"
    local cli="
      if (outcome != success) of ${subsystemAddr}:read-resource
        echo You have set the DEFAULT_JOB_REPOSITORY environment variables to configure a default-job-repository pointing to the '${DEFAULT_JOB_REPOSITORY}' datasource. Fix your configuration to contain a batch-jberet subsystem for this to happen. >> \${error_file}
        exit
      end-if

      if (outcome == success) of ${resourceAddr}:read-resource
        batch
        ${resourceAddr}:remove
        ${resourceAddr}:add(data-source=${1})
        run-batch
      else
        ${resourceAddr}:add(data-source=${1})
      end-if
    "
    # Since this is happening as part of the datasource generation, in a subshell, and these
    # commands need to happen AFTER the datasource has been added, write the CLI commands out
    # to a temp file that we will read later
    echo "${cli}" >> "${DEFAULT_JOB_REPOSITORY_FILE}"
  fi

}
