import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const iconAliases = {
  BarChart3: 'DataAnalysis',
  Dashboard: 'DataAnalysis',
  LayoutDashboard: 'DataAnalysis',
  Building: 'OfficeBuilding',
  Home: 'House',
  Layout: 'Grid',
  Video: 'VideoCamera',
  Users: 'User',
  UserCog: 'Setting'
}

export const resolveMenuIcon = (icon) => {
  if (!icon) return null

  const iconName = iconAliases[icon] || icon
  return ElementPlusIconsVue[iconName] || null
}

export const menuIconOptions = [
  { value: 'DataAnalysis', label: 'DataAnalysis' },
  { value: 'Setting', label: 'Setting' },
  { value: 'Menu', label: 'Menu' },
  { value: 'User', label: 'User' },
  { value: 'UserFilled', label: 'UserFilled' },
  { value: 'House', label: 'House' },
  { value: 'OfficeBuilding', label: 'OfficeBuilding' },
  { value: 'Grid', label: 'Grid' },
  { value: 'VideoCamera', label: 'VideoCamera' },
  { value: 'Calendar', label: 'Calendar' },
  { value: 'Monitor', label: 'Monitor' },
  { value: 'Message', label: 'Message' },
  { value: 'Document', label: 'Document' },
  { value: 'Phone', label: 'Phone' },
  { value: 'Star', label: 'Star' },
  { value: 'Search', label: 'Search' },
  { value: 'Bell', label: 'Bell' },
  { value: 'Ticket', label: 'Ticket' }
]
