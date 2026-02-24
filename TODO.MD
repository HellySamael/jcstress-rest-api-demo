==> Virer Dagger
==> Clean le code (archi)



=> Prez
==> PreZ rapide de JCStress
==> Cas concret (application qui compte des likes avec schema) + explication de code
==> Identifier qu'on a un probleme => ConcurentLikeClient => dire que ça n'explique pas le pb exactement => feedback loop longue
==> Fix avec les volatiles, atomique, synchronized ... 

==> Exemple:
=> order book
=> Event bus 
=> Generation d'ids unique
=> Cache distrib

==>https://github.com/amoffet/jcstress/blob/d8e24b869187a189e2ddced07be18bc9c8e06201/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/high/Concurrency_01_CHM.java#L25 
Voir si on peut essayer avec un hashmap