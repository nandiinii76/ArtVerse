import { Client } from '@stomp/stompjs'

const apiUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'
const httpBase = apiUrl.replace(/\/api\/v1\/?$/, '')
const wsBase = httpBase.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:')

export function connectAuction(auctionId: string, onEvent: (event: unknown) => void) {
  const client = new Client({ brokerURL: `${wsBase}/ws`, reconnectDelay: 3000 })
  client.onConnect = () => {
    client.subscribe(`/topic/auctions/${auctionId}`, message => {
      try { onEvent(JSON.parse(message.body)) } catch { /* ignore malformed events */ }
    })
  }
  client.activate()
  return () => { void client.deactivate() }
}

export function connectNotifications(userId: string, onNotification: (notification: unknown) => void) {
  const client = new Client({ brokerURL: `${wsBase}/ws`, reconnectDelay: 5000 })
  client.onConnect = () => {
    client.subscribe(`/user/${userId}/queue/notifications`, message => {
      try { onNotification(JSON.parse(message.body)) } catch { /* ignore malformed events */ }
    })
  }
  client.activate()
  return () => { void client.deactivate() }
}
