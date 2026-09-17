#!/usr/bin/perl
# Homologa los nombres a la escritura CORRECTA, no a la mayoritaria.
#
# La mayoria no sirve de criterio aqui: nueve tiendas tienen la ñ doblemente
# codificada -bytes C383C2B1, que es el UTF-8 de "Ã±"- y solo dos la tienen
# bien. Homologar por mayoria rompe justo las dos que estan bien.
#
# El criterio es, en orden:
#   1. que los bytes sean UTF-8 valido
#   2. que NO tengan la firma de doble codificacion -Ã o Â-
#   3. que tengan mas letras acentuadas -"Piña" gana sobre "Pina"-
#   4. y solo entonces, cuantas tiendas lo tienen
use strict;
use warnings;
use Encode qw(decode);

my $dir    = shift;
my $salida = shift;
my $modo   = shift // 'informe';
my @tiendas = qw(Manrique Bello America Calasanz Itagui LaMota Envigado Pilarica SanAntonio ManriquePiloto Niquia);

# Ids donde el nombre distinto NO es escritura sino otro producto o una
# decision de operacion.
my %noTocar = (
    'prod_411' => 'Domicilio $3.000 contra $2.000: es un precio',
    'prod_429' => 'GD Doble Online PV contra Grande DIDI: otro producto',
    'prod_453' => 'Manzana contra Pepsi Pet 250 mL: otro producto',
    'prod_507' => 'Soda Saborizada Segunda 50% contra 2x1: otra promocion',
);

sub leer {
    my ($archivo) = @_;
    my %h;
    open(my $fh, '<', $archivo) or die "$archivo: $!";
    while (<$fh>) {
        tr/\r//d;
        chomp;
        my ($id, $hex) = split(/\t/, $_, 2);
        next unless defined $hex;
        $h{$id} = $hex;
    }
    close $fh;
    return (\%h);
}

# Que tan buena es una escritura. Mas alto es mejor.
sub calificar {
    my ($hex, $votos) = @_;
    my $bytes = pack('H*', $hex);

    my $texto = eval { decode('UTF-8', $bytes, Encode::FB_CROAK) };
    #Bytes que no son UTF-8 valido: lo peor, se descarta salvo que no haya otra.
    return (0 + $votos) if !defined $texto;

    #Firma de doble codificacion. Una ñ bien escrita nunca trae Ã ni Â.
    my $mojibake = ($texto =~ /[\x{C3}\x{C2}]/) ? 1 : 0;

    #Letras acentuadas de verdad.
    my $acentos = () = $texto =~ /[\x{E0}-\x{FF}\x{100}-\x{17F}]/g;

    return (10000 - ($mojibake * 5000) + ($acentos * 100) + $votos);
}

my (%sql, @informe, @saltados);
my $cambios = 0;

for my $caso (['prod', 'producto', 'idproducto', 'descripcion'],
              ['item', 'item_inventario', 'iditem', 'nombre_item']) {
    my ($pref, $tabla, $llave, $campo) = @{$caso};

    my %datos;
    for my $t (@tiendas) { $datos{$t} = leer("$dir/${pref}hex_$t.tsv"); }

    my %ids;
    for my $t (@tiendas) { for my $k (keys %{$datos{$t}}) { $ids{$k} = 1; } }

    for my $id (sort { $a <=> $b } keys %ids) {
        my %cuenta;
        my $presentes = 0;
        for my $t (@tiendas) {
            next unless exists $datos{$t}{$id};
            $presentes++;
            $cuenta{$datos{$t}{$id}}++;
        }
        next if $presentes < scalar(@tiendas);
        next if scalar(keys %cuenta) == 1;

        if (exists $noTocar{"${pref}_$id"}) {
            push @saltados, sprintf("%-16s %-6s %s", $tabla, $id, $noTocar{"${pref}_$id"});
            next;
        }

        my ($mejor, $mejorNota);
        for my $hex (sort keys %cuenta) {
            my $nota = calificar($hex, $cuenta{$hex});
            if (!defined $mejorNota || $nota > $mejorNota) {
                ($mejor, $mejorNota) = ($hex, $nota);
            }
        }

        my @cambian = grep { $datos{$_}{$id} ne $mejor } @tiendas;
        $cambios += scalar(@cambian);

        my $texto = eval { decode('UTF-8', pack('H*', $mejor)) } // '(bytes raros)';
        my @otras;
        for my $hex (sort { $cuenta{$b} <=> $cuenta{$a} } keys %cuenta) {
            next if $hex eq $mejor;
            my $t2 = eval { decode('UTF-8', pack('H*', $hex)) } // pack('H*', $hex);
            push @otras, "$t2 ($cuenta{$hex})";
        }
        push @informe, sprintf("%-16s %-6s QUEDA: %-30s  (lo tenian %d)   ANTES: %s",
            $tabla, $id, $texto, $cuenta{$mejor}, join(' | ', @otras));

        for my $t (@cambian) {
            push @{$sql{$t}}, sprintf(
                "-- %s %s -> %s\nUPDATE %s SET %s = UNHEX('%s') WHERE %s = %s;",
                $tabla, $id, $texto, $tabla, $campo, $mejor, $llave, $id);
        }
    }
}

binmode(STDOUT, ':encoding(UTF-8)');
print "$_\n" for @informe;
print "\nTOTAL de filas a cambiar: $cambios\n";
print "\nNO SE TOCAN:\n";
print "  $_\n" for @saltados;
print "\nCambios por tienda:\n";
printf("  %-17s %3d\n", $_, scalar(@{$sql{$_} // []})) for @tiendas;

if ($modo eq 'generar') {
    mkdir $salida unless -d $salida;
    for my $t (@tiendas) {
        next unless $sql{$t};
        open(my $out, '>', "$salida/nombres_$t.sql") or die $!;
        print $out "-- ---------------------------------------------------------------------------\n";
        print $out "-- HOMOLOGACION DE NOMBRES - $t\n";
        print $out "--\n";
        print $out "-- Se corre en la base de ESTA tienda (tiendaamericana).\n";
        print $out "-- Solo cambia texto: no toca ids, ni precios, ni recetas.\n";
        print $out "--\n";
        print $out "-- Se homologa a la escritura CORRECTA, NO a la mayoritaria. Nueve tiendas\n";
        print $out "-- tienen la enye doblemente codificada -bytes C383C2B1, el UTF-8 de \"A~\"- y\n";
        print $out "-- solo dos la tienen bien: homologar por mayoria rompia las dos buenas.\n";
        print $out "--\n";
        print $out "-- El valor se escribe con UNHEX de los bytes buenos. Escribirlo como texto\n";
        print $out "-- obligaria a que cliente, conexion y columna coincidan en codificacion, y\n";
        print $out "-- es justo lo que esta mal aqui.\n";
        print $out "--\n";
        print $out "-- Cambios en esta tienda: " . scalar(@{$sql{$t}}) . "\n";
        print $out "-- ---------------------------------------------------------------------------\n\n";
        print $out "START TRANSACTION;\n\n";
        print $out "$_\n\n" for @{$sql{$t}};
        print $out "COMMIT;\n";
        close $out;
    }
    print "\nScripts generados en $salida\n";
}
