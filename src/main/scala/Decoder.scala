import Types.{Bit, Digit, Even, NoParity, Odd, One, Parity, Pixel, Str, Zero}

import scala.annotation.tailrec
import scala.collection.immutable

object Decoder {
    /** 1.1
     * Converteste un caracter (Char) intr un tip Bit
     *
     * @param s caracterul de intrare
     * @return daca s este '0' se returneaza Zero; daca s este '1' se returneaza One
     */
    def toBit(s: Char): Bit = s match {
        case '0' => Zero
        case '1' => One
    }

    /**
     * Converteste un numar intreg (Int) intr un tip Bit
     *
     * @param s numarul intreg de intrare
     * @return daca s este 0 se returneaza Zero; daca s este 1 se returneaza One
     */
    def toBit(s: Int): Bit = s match {
        case 0 => Zero
        case 1 => One
    }

    /** 1.2
     * Inverseaza un bit (Bit) dat
     *
     * @param c bitul de intrare
     * @return Zero daca c este One; One daca c este Zero
     */
    def complement(c: Bit): Bit = c match {
        case Zero => One
        case One => Zero
    }

    /** 1.3
     */
    val LStrings: List[String] = List("0001101", "0011001", "0010011", "0111101", "0100011",
        "0110001", "0101111", "0111011", "0110111", "0001011")
    val leftOddList: List[List[Bit]] = LStrings.map(_.map(toBit).toList)    // codificări L
    val rightList: List[List[Bit]] = leftOddList.map(_.map(complement))     // codificari R
    val leftEvenList: List[List[Bit]] = rightList.map(_.reverse)            // codificari G

    /** 1.4
     * Grupeaza elementele egale si consecutive dintr o lista intr o lista de liste
     *
     * @param l lista de elemente pe care le grupam
     * @return o lista de liste, fiecare sublista contine grupul de elemente consecutive egale
     */
    def group[A](l: List[A]): List[List[A]] = {
        /**
         * Preia elementele consecutive egale cu primul element din lista
         *
         * @param lst lista de elemente
         * @param first primul element
         * @return o pereche (lista de elemente preluate, restul listei)
         */
        def takeSame(lst: List[A], first: A): (List[A], List[A]) = lst match {
            case Nil => (Nil, Nil)
            case h :: t =>
                if (h == first) {
                    val (same, rest) = takeSame(t, first)
                    (h :: same, rest)
                } else {
                    (Nil, lst)
                }
        }

        l match {
            case Nil => Nil
            case h :: t =>
                val (grouped, rest) = takeSame(l, h)
                grouped :: group(rest)
        }
    }

    /** 1.5
     * Transforma o lista intr o lista de perechi (numar, element),
     * fiecare pereche reprezinta cate elemente consecutive au fost gasite
     *
     * @param l lista de elemente
     * @return lista de perechi (numar aparitii, element)
     */
    def runLength[A](l: List[A]): List[(Int, A)] = group(l).map(g => (g.length, g.head))

    case class RatioInt(n: Int, d: Int) extends Ordered[RatioInt] {
        require(d != 0, "Denominator cannot be zero")
        private val gcd = BigInt(n).gcd(BigInt(d)).toInt
        val a = n / gcd // numărător
        val b = d / gcd // numitor

        override def toString: String = s"$a/$b"

        override def equals(obj: Any): Boolean = obj match {
            case that: RatioInt => this.a.abs == that.a.abs &&
                this.b.abs == that.b.abs &&
                this.a.sign * this.b.sign == that.a.sign * that.b.sign
            case _ => false
        }

        /** 2.1
         * Implementarea operatorilor pentru scadere, adunare, inmultire si impartire
         *
         * @param other fractia cu care operam
         * @return o noua fractie rezultata din operatia efectuata
         */
        def -(other: RatioInt): RatioInt = RatioInt(this.a * other.b - this.b * other.a, this.b * other.b)
        def +(other: RatioInt): RatioInt = RatioInt(this.a * other.b + this.b * other.a, this.b * other.b)
        def *(other: RatioInt): RatioInt = RatioInt(this.a * other.a, this.b * other.b)
        def /(other: RatioInt): RatioInt = RatioInt(this.a * other.b, this.b * other.a)

        /** 2.2
         * Compararea a doua fractii
         *
         * @param other fractia cu care comparam
         * @return -1 daca this < other, 0 daca this == other, 1 daca this > other
         */
        def compare(other: RatioInt): Int = {
            val left = this.a * other.b
            val right = this.b * other.a
            if (left < right) -1
            else if (left > right) 1
            else 0
        }
    }

    /** 3.1
     * Transforma o lista de perechi (numar aparitii, element) intr o lista de perechi (ratio, element),
     * ratio este numarul curent impartit la suma totala a numerelor
     *
     * @param l lista de perechi (numar aparitii, element)
     * @return lista de perechi (ratio, element)
     */
    def scaleToOne[A](l: List[(Int, A)]): List[(RatioInt, A)] = {
        val total = l.map(_._1).sum
        l.map { case (count, value) =>
            val ratio = RatioInt(count, total)
            (ratio, value)
        }
    }

    /** 3.2
     * Transforma o lista de perechi (numar, bit) intr o pereche (bit, lista de ratio),
     * bitul este primul element din lista si lista de ratio este obtinuta prin scaleToOne
     *
     * @param l lista de perechi (numar, bit)
     * @return pereche (bit, lista de ratio)
     */
    def scaledRunLength(l: List[(Int, Bit)]): (Bit, List[RatioInt]) = {
        val scaled = scaleToOne(l)
        val firstBit = l.head._2
        val freqs = scaled.map(_._1)
        (firstBit, freqs)
    }

    /** 3.3
     * Transforma un string intr o lista de Parity
     *
     * @param s stringul de intrare
     * @return lista de Parity; Odd pentru 'L', Even pentru 'G'
     */
    def toParities(s: Str): List[Parity] = {
        s.map {
            case 'L' => Odd
            case 'G' => Even
        }
    }

    /** 3.4
     */
    val PStrings: List[String] = List("LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
        "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL")
    val leftParityList: List[List[Parity]] = PStrings.map(_.toList).map(toParities)

    /** 3.5
     */
    type SRL = (Bit, List[RatioInt])
    val leftOddSRL:  List[SRL] = leftOddList.map(runLength).map(scaledRunLength)
    val leftEvenSRL:  List[SRL] = leftEvenList.map(runLength).map(scaledRunLength)
    val rightSRL:  List[SRL] = rightList.map(runLength).map(scaledRunLength)

    /** 4.1
     * Calculeaza distanta dintre doua coduri SRL
     *
     * @param l1 primul cod SRL
     * @param l2 al doilea cod SRL
     * @return distanta dintre cele doua coduri SRL sub forma de RatioInt
     */
    def distance(l1: SRL, l2: SRL): RatioInt = {
        val (bit1, r1) = l1
        val (bit2, r2) = l2

        if (bit1 != bit2) RatioInt(100, 1)
        else r1.zip(r2).map { case (a, b) =>
            val diff = a - b
            RatioInt(math.abs(diff.a), diff.b)
        }.reduce(_ + _)
    }

    /** 4.2
     * Gaseste cel mai bun cod SRL care se potriveste cu un cod SRL dat
     *
     * @param SRL_Codes lista de coduri SRL
     * @param digitCode codul SRL de cautat
     * @return o pereche (distanta, cifra) care reprezinta cea mai buna potrivire
     */
    def bestMatch(SRL_Codes: List[SRL], digitCode: SRL): (RatioInt, Digit) = {
        SRL_Codes.zipWithIndex.map {
            case (srl, digit) => (distance(srl, digitCode), digit)
        }.minBy(_._1)
    }

    /** 4.3
     * Gaseste cel mai bun cod SRL din stanga care se potriveste cu un cod SRL dat
     *
     * @param digitCode codul SRL de cautat
     * @return o pereche (paritate, cifra) care reprezinta cea mai buna potrivire
     */
    def bestLeft(digitCode: SRL): (Parity, Digit) = {
        val (dOdd, digitOdd) = bestMatch(leftOddSRL, digitCode)
        val (dEven, digitEven) = bestMatch(leftEvenSRL, digitCode)
        if (dOdd < dEven) (Odd, digitOdd) else (Even, digitEven)
    }

    /** 4.4
     * Gaseste cel mai bun cod SRL din dreapta care se potriveste cu un cod SRL dat
     *
     * @param digitCode codul SRL de cautat
     * @return o pereche (paritate, cifra) care reprezinta cea mai buna potrivire
     */
    def bestRight(digitCode: SRL): (Parity, Digit) = {
        val (_, digit) = bestMatch(rightSRL, digitCode)
        (NoParity, digit)
    }

    def chunkWith[A](f: List[A] => (List[A], List[A]))(l: List[A]): List[List[A]] = {
        l match {
            case Nil => Nil
            case _ =>
                val (h, t) = f(l)
                h :: chunkWith(f)(t)
        }
    }

    def chunksOf[A](n: Int)(l: List[A]): List[List[A]] =
        chunkWith((l: List[A]) => l.splitAt(n))(l)

    /** 4.5
     * Gaseste ultimele 12 cifre dintr o lista de perechi (numar, bit)
     *
     * @param rle lista de perechi (numar, bit)
     * @return lista de perechi (paritate, cifra) corespunzatoare ultimelor 12 cifre
     */
    def findLast12Digits(rle: List[(Int, Bit)]): List[(Parity, Digit)] = {
        val leftBars = rle.drop(3).take(24)
        val rightBars = rle.drop(32).take(24)

        val digitBars = leftBars ++ rightBars
        val groupsOf4 = chunksOf(4)(digitBars)

        val leftDigits = groupsOf4.take(6).map(scaledRunLength).map(bestLeft)
        val rightDigits = groupsOf4.drop(6).take(6).map(scaledRunLength).map(bestRight)

        leftDigits ++ rightDigits
    }

    /** 4.6
     * Determina prima cifra dintr o lista de perechi (paritate, cifra)
     *
     * @param l lista de perechi (paritate, cifra)
     * @return prima cifra
     */
    def firstDigit(l: List[(Parity, Digit)]): Option[Digit] = {
        val parities = l.take(6).map(_._1)
        leftParityList.zipWithIndex.find { case (pList, _) => pList == parities } match {
            case Some((_, idx)) => Some(idx)
            case None => None
        }
    }

    /** 4.7
     * Calculeaza cifra de control pentru un cod de bare dat
     *
     * @param digits lista de cifre
     * @return cifra de control
     */
    def checkDigit(digits: List[Digit]): Digit = {
        val sum = digits.take(12).zipWithIndex.map { case (d, idx) =>
            val weight = if ((idx + 1) % 2 == 0) 3 else 1
            d * weight
        }.sum

        val remainder = sum % 10
        if (remainder == 0) 0 else 10 - remainder
    }

    /** 4.8
     * Verifica un cod de bare dat
     *
     * @param code lista de perechi (paritate, cifra)
     * @return Some(codul) daca codul este valid; None altfel
     */
    def verifyCode(code: List[(Parity, Digit)]): Option[String] = {
        val codeWithoutFirst = code.tail
        firstDigit(codeWithoutFirst.take(6)) match {
            case Some(first) =>
                val digits: List[Digit] = first :: codeWithoutFirst.map(_._2)
                val computedControl = checkDigit(digits.take(12))
                if (computedControl == digits(12)) Some(digits.mkString(""))
                else None
            case None =>
                None
        }
    }

    /** 4.9
     * Rezolva un cod de bare dat
     *
     * @param rle lista de perechi (numar, bit)
     * @return Some(codul) daca codul este valid; None altfel
     */
    def solve(rle: List[(Int, Bit)]): Option[String] = {
        val last12 = findLast12Digits(rle)
        firstDigit(last12.take(6)) match {
            case Some(first) =>
                val fullCode = (NoParity, first) :: last12
                verifyCode(fullCode)
            case None =>
                None
        }
    }

    def checkRow(row: List[Pixel]): List[List[(Int, Bit)]] = {
        val rle = runLength(row);

        def condition(sl: List[(Int, Pixel)]): Boolean = {
            if (sl.isEmpty) false
            else if (sl.size < 59) false
            else sl.head._2 == 1 &&
                sl.head._1 == sl.drop(2).head._1 &&
                sl.drop(56).head._1 == sl.drop(58).head._1
        }

        rle.sliding(59, 1)
            .filter(condition)
            .toList
            .map(_.map(pair => (pair._1, toBit(pair._2))))
    }
}