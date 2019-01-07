package tech.pegasys.pantheon.ethereum.p2p.permissioning;

import com.github.jgonian.ipmath.Ipv4;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6;
import com.github.jgonian.ipmath.Ipv6Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;
import tech.pegasys.pantheon.ethereum.permissioning.PermissioningConfiguration;

public class NetworkWhitelistController {
  private final Collection<Ipv4Range> ipv4Masks;
  private final Collection<Ipv6Range> ipv6Masks;
  private boolean networkWhitelistSet = false;

  public NetworkWhitelistController(final PermissioningConfiguration configuration) {
    ipv4Masks = new ArrayList<>();
    ipv6Masks = new ArrayList<>();
  }

  private boolean manipulateMaskLists(final String mask, final Predicate<Ipv4Range> ipv4Manipulation, final Predicate<Ipv6Range> ipv6Manipulation) {
    try {
      final Ipv4 singleIp = Ipv4.parse(mask);
      return ipv4Manipulation.test(Ipv4Range.from(singleIp).andPrefixLength(32));
    } catch (final IllegalArgumentException ipv4Exception) {
      try {
        final Ipv4Range range = Ipv4Range.parse(mask);
        return ipv4Manipulation.test(range);
      } catch (final IllegalArgumentException ipv4RangeException) {
        try {
          final Ipv6 singleIp = Ipv6.parse(mask);
          return ipv6Manipulation.test(Ipv6Range.from(singleIp).andPrefixLength(32));
        } catch (final IllegalArgumentException ipv6Exception) {
          try {
            final Ipv6Range range = Ipv6Range.parse(mask);
            return ipv6Manipulation.test(range);
          } catch (final IllegalArgumentException ipv6RangeException) {
            throw new IllegalArgumentException("Unrecognised ip mask format");
          }
        }
      }
    }
  }

  public boolean addIpMask(final String mask) {
    if (manipulateMaskLists(mask, ipv4Masks::add, ipv6Masks::add)) {
      networkWhitelistSet = true;
      return true;
    }
    return false;
  }


  public boolean removeIpMask(final String mask) {
    return manipulateMaskLists(mask, ipv4Masks::remove, ipv6Masks::remove);
  }

  public boolean contains(final Peer node) {
    if (networkWhitelistSet) {
      try {
        final Ipv4 ipv4Host = Ipv4.of(node.getEndpoint().getHost());
        return ipv4Masks.stream().anyMatch(mask -> mask.contains(ipv4Host));
      } catch (final IllegalArgumentException ipv4Exception) {
        final Ipv6 ipv6Host = Ipv6.of(node.getEndpoint().getHost());
        return ipv6Masks.stream().anyMatch(mask -> mask.contains(ipv6Host));
      }
    } else {
      return true;
    }
  }
}
