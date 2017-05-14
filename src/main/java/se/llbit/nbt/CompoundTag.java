/* Copyright (c) 2010-2017, Jesper Öqvist
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package se.llbit.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompoundTag extends SpecificTag implements Iterable<Tag> {
  final List<Tag> items;

  public void add(String name, SpecificTag tag) {
    add(new NamedTag(new StringTag(name), tag));
  }

  public static SpecificTag read(DataInputStream in) {
    CompoundTag tagThis = new CompoundTag();
    while (true) {
      Tag last = NamedTag.read(in);
      if (last.isEnd()) {
        break;
      }

      tagThis.add(last);
    }
    return tagThis;
  }

  @Override public void write(DataOutputStream out) throws IOException {
    for (Tag item : items) {
      item.write(out);
    }
    out.writeByte(Tag.TAG_END);
  }

  static Map<String, Tag> partialParse(DataInputStream in, String prefix,
      Map<String, Tag> result, Set<String> request, Set<String> prefixes) {
    try {
      while (true) {
        byte type = in.readByte();
        if (type == Tag.TAG_END) {
          break;
        }
        SpecificTag name = StringTag.read(in);
        String tag = prefix + "." + name.stringValue();
        boolean parsed = NamedTag.partiallyParseTag(in, result, request, prefixes, type, tag);
        if (parsed) {
          if (request.isEmpty()) {
            return result;
          }
        } else {
          SpecificTag.skip(type, in);
        }
      }
    } catch (IOException e) {
    }
    return result;
  }

  static void skip(DataInputStream in) {
    try {
      while (true) {
        byte itemType = in.readByte();
        if (itemType == 0) {
          break;
        }

        StringTag.skip(in);
        SpecificTag.skip(itemType, in);
      }
    } catch (IOException e) {
    }
  }

  public CompoundTag() {
    items = new ArrayList<>();
  }

  @Override public void dumpTree(StringBuilder buff, String indent) {
    buff.append(indent);
    dumpTree(buff);
    for (Tag item : items) {
      item.dumpTree(buff, indent + "  ");
    }
  }

  public CompoundTag(List<Tag> items) {
    this.items = new ArrayList<>(items);
  }

  /**
   * The number of items in this compound tag.
   */
  public int size() {
    return items.size();
  }

  /**
   * Append an item to this compound tag.
   */
  public void add(Tag node) {
    items.add(node);
  }

  public String toString() {
    return dumpTree();
  }

  @Override public String type() {
    return "TAG_Compound";
  }

  @Override public String name() {
    return "TAG_Compound";
  }

  @Override public int tagType() {
    return Tag.TAG_COMPOUND;
  }

  @Override public boolean isCompoundTag() {
    return true;
  }

  @Override public Tag get(String name) {
    for (Tag item : items) {
      if (item.isNamed(name)) {
        return item.unpack();
      }
    }
    return new ErrorTag("No item named \"" + name + "\" in this compound tag.");
  }

  @Override public void set(String name, Tag tag) {
    // TODO
    throw new Error();
  }

  @Override public Iterator<Tag> iterator() {
    return items.iterator();
  }
}
