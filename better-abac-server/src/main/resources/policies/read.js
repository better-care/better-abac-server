function personalPhysician(ctx) {
    return policyHelper.hasRelation(ctx.user, 'PERSONAL_PHYSICIAN', ctx.patient);
}

function personOfTrust(ctx) {
    return policyHelper.hasRelation(ctx.user, 'PERSON_OF_TRUST', ctx.patient);
}

function wardPhysician(ctx) {
    var relations = policyHelper.getRelation(ctx.patient, 'ON_WARD');
    return policyHelper.hasRelation(ctx.user, 'MEMBER_OF', relations);
}

function policy(ctx) {
    return personalPhysician(ctx) || personOfTrust(ctx) || wardPhysician(ctx);
}