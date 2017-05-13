/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

/**
 *
 * @author ichigo
 */
public class Film 
{
    private String nombre_original;
    private String nombre;
    
    public Film(String nombre1, String nombre2)
    {
        this.nombre = nombre2;
        this.nombre_original = nombre1;
    }
    
    //  Esto podría no estar en la bd
    public String getNombreOriginal()
    {
        return this.nombre_original;
    }
    
    //  Este nombre está sí o sí
    public String getNombre()
    {
        return this.nombre;
    }
}
