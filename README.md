# DNS-Client

The program sends a DNS query to an authority server using a UDP socket and interprets the response packet to output the response in a readable format.

The DNS Client is able to send queries for IP addresses, mail servers, and name servers. It can also retransmit queries that are lost. It also has error handling functionality that returns back an error message for common errors such as responses that do not match the query or fields or entries that cannot be interpreted. 

Calling syntax: 
```
java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name
```

where the arguments are defined as follows: 
* timeout (optional) gives how long to wait, in seconds, before retransmitting an
unanswered query. Default value: 5.
* max-retries (optional) is the maximum number of times to retransmit an unanswered
query before giving up. Default value: 3.
* port (optional) is the UDP port number of the DNS server. Default value: 53.
* -mx or -ns flags (optional) indicate whether to send a MX (mail server) or NS (name server) query. At most one of these can be given, and if neither is given then the client sends a type A (IP address) query.
* server (required) is the IPv4 address of the DNS server, in a.b.c.d. format
* name (required) is the domain name to query for.

## Example Queries

Query for www.mcgill.ca IP address using the McGill DNS server:
```
java DnsClient @132.206.85.18 www.mcgill.ca
```

Query for the mcgill.ca mail server using Google’s public DNS server with a timeout of 10 seconds and at most 2 retries:
```
java DnsClient –t 10 –r 2 –mx @8.8.8.8 mcgill.ca
```

## Output of DNS Client

```
DnsClient sending request for [name] Server: [server IP address]
Request type: [A | MX | NS]
```

If a response is received, the following format is displayed:
```
Response received after [time] seconds ([num-retries] retries)
```
If the response contains records in the Answer section, then the following is displayed: 
```
***Answer Section ([num-answers] records)***
```
Then, each record is printed in a specific format according to its type:
* IP addresses
```
IP <tab> [ip address] <tab> [seconds can cache] <tab> [auth | nonauth]
```
* CNAME records 
```
CNAME <tab> [alias] <tab> [seconds can cache] <tab> [auth | nonauth]
```
* MX records
```
MX <tab> [alias] <tab> [pref] <tab> [seconds can cache] <tab> [auth | nonauth]
```
* NS records
```
NS <tab> [alias] <tab> [seconds can cache] <tab> [auth | nonauth]
```
If the response contains records in the Additional section then it also prints:
```
***Additional Section ([num-additional] records)***
```

along with appropriate lines for each additional record that matches one of the types A, CNAME, MX, or NS. Records in the authority section are ignored.

If no record is found then the Client displays a ```NOTFOUND``` message. 

For any errors during program execution, the Client prints an adequate description of what went wrong
