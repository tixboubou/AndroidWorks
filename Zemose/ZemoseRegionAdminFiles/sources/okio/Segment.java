package okio;

import javax.annotation.Nullable;

final class Segment {
    static final int SHARE_MINIMUM = 1024;
    static final int SIZE = 8192;

    /* renamed from: data reason: collision with root package name */
    final byte[] f16data;
    int limit;
    Segment next;
    boolean owner;
    int pos;
    Segment prev;
    boolean shared;

    Segment() {
        this.f16data = new byte[8192];
        this.owner = true;
        this.shared = false;
    }

    Segment(byte[] data2, int pos2, int limit2, boolean shared2, boolean owner2) {
        this.f16data = data2;
        this.pos = pos2;
        this.limit = limit2;
        this.shared = shared2;
        this.owner = owner2;
    }

    /* access modifiers changed from: 0000 */
    public Segment sharedCopy() {
        this.shared = true;
        Segment segment = new Segment(this.f16data, this.pos, this.limit, true, false);
        return segment;
    }

    /* access modifiers changed from: 0000 */
    public Segment unsharedCopy() {
        Segment segment = new Segment((byte[]) this.f16data.clone(), this.pos, this.limit, false, true);
        return segment;
    }

    @Nullable
    public Segment pop() {
        Segment result = this.next;
        if (result == this) {
            result = null;
        }
        Segment segment = this.prev;
        segment.next = this.next;
        this.next.prev = segment;
        this.next = null;
        this.prev = null;
        return result;
    }

    public Segment push(Segment segment) {
        segment.prev = this;
        segment.next = this.next;
        this.next.prev = segment;
        this.next = segment;
        return segment;
    }

    public Segment split(int byteCount) {
        Segment prefix;
        if (byteCount <= 0 || byteCount > this.limit - this.pos) {
            throw new IllegalArgumentException();
        }
        if (byteCount >= 1024) {
            prefix = sharedCopy();
        } else {
            prefix = SegmentPool.take();
            System.arraycopy(this.f16data, this.pos, prefix.f16data, 0, byteCount);
        }
        prefix.limit = prefix.pos + byteCount;
        this.pos += byteCount;
        this.prev.push(prefix);
        return prefix;
    }

    public void compact() {
        Segment segment = this.prev;
        if (segment == this) {
            throw new IllegalStateException();
        } else if (segment.owner) {
            int byteCount = this.limit - this.pos;
            if (byteCount <= (8192 - segment.limit) + (segment.shared ? 0 : segment.pos)) {
                writeTo(this.prev, byteCount);
                pop();
                SegmentPool.recycle(this);
            }
        }
    }

    public void writeTo(Segment sink, int byteCount) {
        if (sink.owner) {
            int i = sink.limit;
            if (i + byteCount > 8192) {
                if (!sink.shared) {
                    int i2 = i + byteCount;
                    int i3 = sink.pos;
                    if (i2 - i3 <= 8192) {
                        byte[] bArr = sink.f16data;
                        System.arraycopy(bArr, i3, bArr, 0, i - i3);
                        sink.limit -= sink.pos;
                        sink.pos = 0;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
            System.arraycopy(this.f16data, this.pos, sink.f16data, sink.limit, byteCount);
            sink.limit += byteCount;
            this.pos += byteCount;
            return;
        }
        throw new IllegalArgumentException();
    }
}
