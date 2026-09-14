import { Client } from '@stomp/stompjs'

let client: Client | null = null

export function connectNotifications(userId: string, onNotification: (notification: unknown) => void) {
  if (client?.active) return () => undefined
  client = new Client({ brokerURL: `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws` })
  client.onConnect = () => {
    client?.subscribe(`/user/${userId}/queue/notifications`, message => {
      try { onNotification(JSON.parse(message.body)) } catch { /* ignore malformed events */ }
    })
  }
  client.activate()
  return () => { void client?.deactivate(); client = null }
}
