# SCOPO DEL PROGETTO
Questa applicazione vuole implementare il gioco del Tris (Tic tac toe in inglese).
Essa consiste in due modalità di gioco distinte:
    - Gioco locale 
    - Gioco multiplayer online

La parte di gioco locale è molto semplice e si alterna tra tre schermate di cui la prima di menu,
la seconda in cui immettere i nomi dei giocatori e poi, premendo il pulsante di avvio gioco, la terza 
che è quella di gioco vero e proprio. Nella game board i due giocatori alternano i propri turni per 
poter piazzare X o O. Una volta che uno vince appare la schermata apposita che lo avvisa in cui
viene anche chiesto se vogliono rigiocare.
È possibile inoltre tornare al menu principale con il tasto "home" oppure cliccando sul tasto back.

// TODO: AGGIUNGI LE IMMAGINI DELLE TRE SCHERMATE


La seconda modalità (online) è molto simile ma, ovviamente, piu complessa dal punto di vista del codice 
per la connessione tra i due dispositivi di cui si parlerà nella parte di **architetturale**.
In breve anche questa modalità conta tre schermate di cui la prima sempre di menu in cui scegliere la modalità.
Una volta scelto Online, i due giocatori dovranno, ognuno dal proprio device, accendere e dare il
permesso all'applicazione di usare Bluetooth e GPS. Vengono prima mostrati dei pop-up illustrativi 
che segnalano agli utenti come mai servono queste due componenti.
Dopodicchè si passa alla seconda schermata, molto simile alla seconda della modalità locale, in cui
il giocatore scrive il proprio nome.
Infine, per accedere alla schermata di gioco, i due giocatori dovranno cliccare uno 'host' e l'altro 
'join' e poi rendersi visibili entrambi. Questo permetterà di cercare, trovare, accoppiare e connettere
i due device che potranno ora giocare con un'interfaccia totalmente uguale a quella della partita locale.
Essendo che i BluetoothSocket usati necessitano dei due dispositivi che si ricercano nello stesso momento
per poter funzionare, si è deciso che se uno dei due giocatori decide di abbandonare la partita
allora anche all'altro tornerà alla schermata principale di menu.

// TODO: AGGIUNGI LE IMMAGINI DELLE TRE SCHERMATE


# ARCHITETTURA DELLA CONNESSIONE 
Per questo progetto si è voluto utilizzare BluetoothSocket per instaurare la connessione tra i due device.
Essi sono simili ai socket ma sfruttano la tecnologia Bluetooth per poter fare la discovery dei dispositivi
vicini. Questo è il motivo per cui, oltre ai permessi di utilizzo Bluetooth, servono i permessi per il GPS.
Una volta abilitato il permesso di essere trovabile, entrambi i device fanno una chiamata ad una
funzione bloccante in cui resteranno in ascolto per accettare una connessione (in questo caso una in 
quanto servono solo due giocatori e quando accetto una connessione vuol dire che sono connessi insieme due
device). AcceptThread è il thread addetto a questo lavoro ed è paragonabile al lavoro che avviene lato server.
Ora che entrambi i device sono in ascolto bisogna che uno dei due device venga "eletto" come quello
che creerà la parte di richiesta client cosicchè possano poi connettersi. Questa sorta di elezione
avviene grazie all'azione dei giocatori, infatti chi sceglierà di premere 'join' sarà quello che renderà
possibile la scoperta e la connessione all'altro.
Anche nel caso lato client la chiamata a funzione startDiscovery è bloccante ed utilizza molte risorse
quindi verrà chiusa subito dopo aver trovato il dispositivo cercato.
Come fanno però a distinguersi nella grande quantità di dispositivi bluetooth che potrebbero esserci?
Si è scelto innanzitutto una chiave UUID statica (non randomica) per poter creare la connessione rfcomm che 
è il nome stesso dell'applicazione ('TRISONLINE'). Inoltre si è deciso di dare un nome ad entrambi 
i device bluetooth che è quello di 'HT' in questo modo si pensa possa essere molto meno probabile 
connettersi ad un dispositivo sbagliato. Non si esclude che comunque chi sappia di queste specifiche 
possa provare a connettersi senza però avere l'applicazione aperta.
Quando AcceptThread accetta una connessione può sganciarsi dalla parte server e usare come scambio di 
messaggi un oggetto BluetoothSocket (per il server viene usato un BluetoothServerSocket e per il client
viene usato il BluetoothDevice trovato nella discovery). 
Una volta ottenuto il BluetoothSocket i due dispositivi possono finalmente passare alla game board
che nel frattempo era bloccata da un icona di caricamento. Inizia quindi lo scambio di messaggi tra i due.
Si noti che prima veniva differenziato il comportamento a seconda di host o join, dopo che la connessione
è stata eseguita con successo i due device non hanno differenti comportamenti ma possono essere considerati
dei Peer.
ConnectedThread esegue una funzione di read finchè uno dei due dispositivi non si disconnette o fino 
a che non c'è un errore. Quando riceve un messaggio lo passa subito alla parte di View che cerca di 
capire che azione prendere, si è deciso quindi che ci sono cinque tipi di messaggi:
    - "start"; è usato all'inizio per lo scambio dei nomi dei due dispositivi. Quando entrambi si connettono
                si passano un messaggio start che poi viene catturato da un Receiver e spedisce all'altro 
                device il suo nome giocatore.
    - "again"; Se c'è stata una vincita da parte di un giocatore o se viene premuto il tasto 'play again'
                viene mandata l'azione di again cosi da resettare la tabella di gioco ed, eventualmente, 
                giocare un'altra partita.
    - un numero; Questa corrisponde ad una mossa del giocatore avversario. Serve anche a sincronizzare i
                giocatori che cosi sanno quando è il proprio turno. Inoltre il numero equivale al numero
                della casella selezionata e serve quindi a fare un update della situazione nel gioco.
                Dopo aver fatto l'update si cerca anche se uno dei due ha vinto.
    - "disconnect"; Quando uno dei due giocatori vuole tornare alla schermata home/menu. Come si è detto nel 
                    paragrafo prima è sembrata l'azione piu sensata da attuare piuttosto che far tornare il giocatore,
                    rimasto solo nel gioco, alla schermata di ricerca di altri dispositivi. 
    - altro che non sia carattere spazio o null; viene usato per quando si deve ricevere il nome del giocatore
                che tipicamente non si sa di cosa è composto (potrebbe contenere numeri o caratteri speciali).
                Dopo aver catturato questo messaggio si mette il nome acquisito nella textView corrispondente.
