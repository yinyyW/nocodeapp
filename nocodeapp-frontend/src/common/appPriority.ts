export enum APP_PRIORITY {
  DEFAULT = 0,
  GOOD = 99,
}

export const APP_PRIORITY_OPTIONS = [
  { label: '普通应用', value: APP_PRIORITY.DEFAULT },
  { label: '优选应用', value: APP_PRIORITY.GOOD },
]

export const getAppPriorityLabel = (value?: number) => {
  return APP_PRIORITY_OPTIONS.find((item) => item.value === value)?.label ?? '普通应用'
}
