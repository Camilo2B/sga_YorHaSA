import Swal from 'sweetalert2';

// Un "toast": aviso pequeño en una esquina que se cierra solo.
// Swal.mixin crea una versión de Swal con estas opciones ya puestas.
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3500,
    timerProgressBar: true,
});

// Los diálogos usan los botones de Bootstrap en lugar de los colores propios de SweetAlert2
const Dialogo = Swal.mixin({
    buttonsStyling: false,
    customClass: {
        confirmButton: 'btn btn-primary mx-1',
        cancelButton: 'btn btn-outline-secondary mx-1',
    },
});

// Funciones globales: se pueden llamar desde un componente, un servicio o un
// interceptor, porque SweetAlert2 pinta fuera del árbol de React.
export function notificarExito(mensaje: string) {
    void Toast.fire({ icon: 'success', title: mensaje });
}

export function notificarError(mensaje: string) {
    void Toast.fire({ icon: 'error', title: mensaje, timer: 5000 });
}

export function notificarAviso(mensaje: string) {
    void Toast.fire({ icon: 'warning', title: mensaje });
}

/** Diálogo Sí/No. Devuelve true si el usuario confirma. */
export async function confirmarAccion(titulo: string, texto: string): Promise<boolean> {
    const resultado = await Dialogo.fire({
        title: titulo,
        text: texto,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, continuar',
        cancelButtonText: 'No',
    });
    return resultado.isConfirmed;
}

/** Pide el motivo de una cancelación (5 a 300 caracteres, como CancelarReservaRequest). */
export async function pedirMotivo(titulo: string): Promise<string | null> {
    const resultado = await Dialogo.fire({
        title: titulo,
        input: 'textarea',
        inputLabel: 'Motivo',
        inputPlaceholder: 'Escriba el motivo…',
        showCancelButton: true,
        confirmButtonText: 'Cancelar reserva',
        cancelButtonText: 'Volver',
        inputValidator: (valor) =>
            valor.trim().length < 5 || valor.length > 300
                ? 'El motivo debe tener entre 5 y 300 caracteres'
                : null,
    });
    return resultado.isConfirmed ? (resultado.value as string).trim() : null;
}
