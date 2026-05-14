import { ref } from 'vue'

export function useApprovalRefresh() {
  const lastRefreshTime = ref(Date.now())
  const refreshTriggers = ref(0)

  const triggerRefresh = () => {
    refreshTriggers.value++
    lastRefreshTime.value = Date.now()
  }

  return {
    lastRefreshTime,
    refreshTriggers,
    triggerRefresh
  }
}
