{{- define "commonLabels" -}}
    {{- range $k, $v := .Values.global.labels }}
{{ $k }}: {{ $v | quote }}
    {{- end -}}
{{- end -}}

