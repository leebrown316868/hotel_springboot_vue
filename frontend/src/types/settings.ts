export interface RoomTypeConfig {
  name: string
  capacity: number
  basePrice: number
}

export interface RoomTypeStats {
  code: string
  roomCount: number
  availableCount: number
  occupiedCount: number
  cleaningCount: number
  maintenanceCount: number
}

export interface RoomTypeWithStats extends RoomTypeConfig {
  code: string
  roomCount: number
  availableCount: number
}
