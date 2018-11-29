package com.jukusoft.mmo.proxy.frontend.loadbalancer;

/**
* load balancing strategies
 *
 * @see <a href="https://kemptechnologies.com/load-balancer/load-balancing-algorithms-techniques/">https://kemptechnologies.com/load-balancer/load-balancing-algorithms-techniques/</a>
*/
public enum LoadBalancingStrategy {

    ROUND_ROBIN,

    RANDOM,

    LEAST_CONN,

    AGENT_BASED

}
