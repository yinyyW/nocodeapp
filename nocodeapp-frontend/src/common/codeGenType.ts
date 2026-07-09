export enum CODE_GEN_TYPE {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

export const CODE_GEN_TYPE_OPTIONS = [
  { label: '原生 HTML 模式', value: CODE_GEN_TYPE.HTML },
  { label: '原生多文件模式', value: CODE_GEN_TYPE.MULTI_FILE },
  { label: 'Vue 工程模式', value: CODE_GEN_TYPE.VUE_PROJECT },
]

export const getCodeGenTypeLabel = (value?: string) => {
  return CODE_GEN_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? value ?? '-'
}
