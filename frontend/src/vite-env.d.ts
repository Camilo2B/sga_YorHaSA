// Tipos de las variables de entorno propias: así import.meta.env.VITE_API_URL
// es un string conocido por TypeScript, con autocompletado.
interface ImportMetaEnv {
    readonly VITE_API_URL: string;
    readonly VITE_NOMBRE_ALOJAMIENTO: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
