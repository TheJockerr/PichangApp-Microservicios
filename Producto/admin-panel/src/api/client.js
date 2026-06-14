import axios from 'axios'

const baseURL =
  import.meta.env.VITE_API_URL ||
  'https://pichangapp-microservicios-production.up.railway.app'

const client = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

// Interceptor de petición: adjunta el JWT guardado en localStorage.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor de respuesta: solo hace logout si el token está realmente expirado o ausente.
// Un 401 de un servicio secundario (permisos, servicio caído) no debe desconectar al usuario.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401 && window.location.pathname !== '/login') {
      const token = localStorage.getItem('token')
      if (!token) {
        window.dispatchEvent(new Event('auth:logout'))
      } else {
        try {
          const payload = JSON.parse(
            atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
          )
          // Solo desconectar si el token está expirado
          if (payload.exp && Date.now() >= payload.exp * 1000) {
            localStorage.removeItem('token')
            localStorage.removeItem('adminEmail')
            window.dispatchEvent(new Event('auth:logout'))
          }
          // Si el token es válido pero el servicio rechaza, dejar que el componente maneje el error
        } catch {
          localStorage.removeItem('token')
          localStorage.removeItem('adminEmail')
          window.dispatchEvent(new Event('auth:logout'))
        }
      }
    }
    return Promise.reject(error)
  }
)

export default client
