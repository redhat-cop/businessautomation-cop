{{/* Generate environment variable name from YAML key */}}
{{- define "envVariableName" -}}
{{- $key := . }}
{{- printf "CUSTOM_%s" ($key | upper | replace "." "_") }}
{{- end -}}

{{/* Generate name of MS SQL extension image */}}
{{- define "msSqlExtensionImageName" -}}
{{- printf "mssql-%s-%s" .Values.database.version .Values.common.server.image }}
{{- end -}}

{{/* Generate build configuration when required */}}
{{- define "buildServerImage" -}}
{{- $database := .Values.database }}
{{- $namespace := .Values.global.rhpam.namespace }}
{{- if $database.extensionImageStreamTag }}
build:
  extensionImageStreamTag: {{ $database.extensionImageStreamTag }}
  extensionImageStreamTagNamespace: {{ $namespace }}
{{- else if eq $database.driver "mssql" }}
build:
  extensionImageStreamTag: {{ include "msSqlExtensionImageName" . }}:{{ .Values.common.version }}
  extensionImageStreamTagNamespace: {{ $namespace }}
{{- end }}
{{- end -}}


