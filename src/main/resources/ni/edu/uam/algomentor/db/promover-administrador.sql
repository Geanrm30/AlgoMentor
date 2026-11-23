-- Convierte una cuenta existente en administradora.
-- Se ejecuta a mano (psql o pgAdmin) con una cuenta de la base de datos; la aplicacion
-- no ofrece ninguna forma de crear administradores. Cambia el usuario por el tuyo, en
-- minusculas, tal como lo escribiste al crear la cuenta.

UPDATE cuenta SET rol = 'ADMIN' WHERE usuario = 'cambia_este_usuario';

-- Para revertirlo:
-- UPDATE cuenta SET rol = 'USUARIO' WHERE usuario = 'cambia_este_usuario';
