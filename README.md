# Alstrudat - c01 - ifs24010

## Deskripsi Soal
Di Perpustakaan Terlarang Del, terdapat sekumpulan **Fragmen Kebenaran**. Setiap fragmen memiliki sebuah **Nilai Keaslian** (bilangan bulat). Jaringan fragmen ini sangat unik: setiap fragmen (kecuali fragmen pusat nomor 1) selalu disalin dari tepat satu **Fragmen Sumber** yang sudah ada sebelumnya.

Tugas Anda adalah memelihara jaringan ini dan mencari **"Rangkaian Harmoni"**, yaitu sekumpulan fragmen yang saling terhubung secara berkelanjutan (jalur) sedemikian rupa sehingga jumlah total Nilai Keaslian dalam rangkaian tersebut adalah yang paling maksimal di seluruh perpustakaan.

Perpustakaan ini dinamis. Anda harus menangani dua jenis aktivitas:
1. **Restorasi**: Nilai Keaslian suatu fragmen berubah.
2. **Laporan**: Memberikan jumlah total Nilai Keaslian dari Rangkaian Harmoni maksimal saat ini.

**Catatan Penting**: Rangkaian Harmoni harus terdiri dari minimal satu fragmen. Rangkaian ini bisa berupa jalur yang menghubungkan dua fragmen mana pun dalam jaringan melalui hubungan sumber-salinan.

## Format Input
- Baris pertama: `N` (jumlah fragmen) dan `Q` (jumlah aktivitas).
- Baris kedua: `N` buah bilangan bulat `W_i`, Nilai Keaslian awal fragmen 1 sampai N.
- Baris ketiga: `N-1` buah bilangan bulat `P_i`, di mana bilangan ke-i adalah indeks sumber dari fragmen ke-(i+1).
- `Q` baris berikutnya berisi salah satu dari:
  - `1 X V`: Ubah Nilai Keaslian fragmen ke-X menjadi V.
  - `2`: Laporkan nilai harmoni maksimal.

## Format Output
Untuk setiap aktivitas tipe `2`, cetak satu baris berisi nilai harmoni maksimal tersebut.

## Constraints
- $1 \le N, Q \le 2 \times 10^5$
- $-10^9 \le W_i, V \le 10^9$
- Fragmen 1 adalah pusat (root).
- Waktu: 2.0 detik.
- Memori: 512 MB.

## Contoh
**Input**:
```text
5 3
1 2 -5 4 3
1 1 2 2
2
1 3 10
2