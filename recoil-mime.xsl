﻿<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	 xmlns="http://www.freedesktop.org/standards/shared-mime-info">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/formats">
		<mime-info>
			<xsl:for-each select="platform/format/ext[not(. = following::ext) and . != 'RPM']">
				<xsl:sort select="." />
				<xsl:variable name="lc" select="translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
				<mime-type type="image/x-{$lc}">
					<comment><xsl:value-of select="." /> image</comment>
					<!-- based on image/png from freedesktop.org.xml -->
					<comment xml:lang="ar">صورة <xsl:value-of select="." /></comment>
					<comment xml:lang="az"><xsl:value-of select="." /> rəsmi</comment>
					<comment xml:lang="be@latin">Vyjava <xsl:value-of select="." /></comment>
					<comment xml:lang="bg">Изображение — <xsl:value-of select="." /></comment>
					<comment xml:lang="ca">imatge <xsl:value-of select="." /></comment>
					<comment xml:lang="cs">obrázek <xsl:value-of select="." /></comment>
					<comment xml:lang="cy">Delwedd <xsl:value-of select="." /></comment>
					<comment xml:lang="da"><xsl:value-of select="." />-billede</comment>
					<comment xml:lang="de"><xsl:value-of select="." />-Bild</comment>
					<comment xml:lang="el">Εικόνα <xsl:value-of select="." /></comment>
					<comment xml:lang="en_GB"><xsl:value-of select="." /> image</comment>
					<comment xml:lang="eo"><xsl:value-of select="." />-bildo</comment>
					<comment xml:lang="es">imagen <xsl:value-of select="." /></comment>
					<comment xml:lang="eu"><xsl:value-of select="." /> irudia</comment>
					<comment xml:lang="fi"><xsl:value-of select="." />-kuva</comment>
					<comment xml:lang="fo"><xsl:value-of select="." /> mynd</comment>
					<comment xml:lang="fr">image <xsl:value-of select="." /></comment>
					<comment xml:lang="ga">íomhá <xsl:value-of select="." /></comment>
					<comment xml:lang="gl">imaxe <xsl:value-of select="." /></comment>
					<comment xml:lang="he">תמונת <xsl:value-of select="." /></comment>
					<comment xml:lang="hr"><xsl:value-of select="." /> slika</comment>
					<comment xml:lang="hu"><xsl:value-of select="." />-kép</comment>
					<comment xml:lang="ia">Imagine <xsl:value-of select="." /></comment>
					<comment xml:lang="id">Citra <xsl:value-of select="." /></comment>
					<comment xml:lang="it">Immagine <xsl:value-of select="." /></comment>
					<comment xml:lang="ja"><xsl:value-of select="." /> 画像</comment>
					<comment xml:lang="kk"><xsl:value-of select="." /> суреті</comment>
					<comment xml:lang="ko"><xsl:value-of select="." /> 그림</comment>
					<comment xml:lang="lt"><xsl:value-of select="." /> paveikslėlis</comment>
					<comment xml:lang="lv"><xsl:value-of select="." /> attēls</comment>
					<comment xml:lang="ms">Imej <xsl:value-of select="." /></comment>
					<comment xml:lang="nb"><xsl:value-of select="." />-bilde</comment>
					<comment xml:lang="nl"><xsl:value-of select="." />-afbeelding</comment>
					<comment xml:lang="nn"><xsl:value-of select="." />-bilete</comment>
					<comment xml:lang="oc">imatge <xsl:value-of select="." /></comment>
					<comment xml:lang="pl">Obraz <xsl:value-of select="." /></comment>
					<comment xml:lang="pt">imagem <xsl:value-of select="." /></comment>
					<comment xml:lang="pt_BR">Imagem <xsl:value-of select="." /></comment>
					<comment xml:lang="ro">Imagine <xsl:value-of select="." /></comment>
					<comment xml:lang="ru">изображение <xsl:value-of select="." /></comment>
					<comment xml:lang="sk">Obrázok <xsl:value-of select="." /></comment>
					<comment xml:lang="sq">Figurë <xsl:value-of select="." /></comment>
					<comment xml:lang="sr"><xsl:value-of select="." /> слика</comment>
					<comment xml:lang="sv"><xsl:value-of select="." />-bild</comment>
					<comment xml:lang="tr"><xsl:value-of select="." /> görüntüsü</comment>
					<comment xml:lang="uk">зображення <xsl:value-of select="." /></comment>
					<comment xml:lang="vi">Ảnh <xsl:value-of select="." /></comment>
					<comment xml:lang="zh_CN"><xsl:value-of select="." /> 图像</comment>
					<comment xml:lang="zh_TW"><xsl:value-of select="." /> 影像</comment>
					<glob pattern="*.{$lc}" />
				</mime-type>
			</xsl:for-each>
		</mime-info>
	</xsl:template>
</xsl:stylesheet>
