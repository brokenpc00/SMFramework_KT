precision mediump float;

uniform sampler2D inputImageTexture;
uniform vec4 inputColor;

#define GAUSSIAN_SAMPLES 9

varying highp vec2 textureCoordinate;
varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];

uniform mediump float distanceNormalizationFactor;

varying vec2 center;
varying float invRadius;
varying float aspect;
varying float aaWidth;

void main()
{
	float dist = distance(textureCoordinate, center) * invRadius;
	float t = min( max( (1.0-dist)/aaWidth, 0.0 ), 1.0 );

    vec4 sum;

	if (dist > 0.0) {
	    vec4 centralColor;
	    float gaussianWeightTotal;
	    vec4 sampleColor;
	    float distanceFromCentralColor;
    	float gaussianWeight;

	    centralColor = texture2D(inputImageTexture, blurCoordinates[4]);
	    gaussianWeightTotal = 0.18;
	    sum = centralColor * 0.18;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[0]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[1]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[2]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[3]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[5]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[6]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[7]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sampleColor = texture2D(inputImageTexture, blurCoordinates[8]);
	    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
	    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);
	    gaussianWeightTotal += gaussianWeight;
	    sum += sampleColor * gaussianWeight;

	    sum /= gaussianWeightTotal;
	} else {
		sum = vec4(0.0, 0.0, 0.0, 0.0);
	}

    gl_FragColor = inputColor * mix(texture2D(inputImageTexture, textureCoordinate), sum, t*0.5);
}
